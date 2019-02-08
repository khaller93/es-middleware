package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This class is an implementation get {@link LDSDWithSPARQLMetricService} using the {@link
 * SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = LDSDWithSPARQLMetricService.UNWEIGHTED_LDSD_SIMILARITY_UID, requiresSPARQL = true,
    prerequisites = {AllResourcesService.class})
public class LDSDWithSPARQLMetricService implements LinkedDataSemanticDistanceMetricService {

  private static final Logger logger = LoggerFactory.getLogger(LDSDWithSPARQLMetricService.class);

  public static final String UNWEIGHTED_LDSD_SIMILARITY_UID = "esm.service.analytics.similarity.ldsd";

  private static final String ALL_LDSD_QUERY =
      "SELECT ?a ?b ((1.0 /(1.0 + COALESCE(?cd,0.0) + COALESCE(?cio,0.0) + COALESCE(?cii,0.0))) as ?ldsd) WHERE {\n"
          + "  OPTIONAL {\n"
          + "    SELECT ?a ?b (COUNT(DISTINCT ?p) as ?cd) WHERE {\n"
          + "      {\n"
          + "        ?a ?p ?b\n"
          + "      } UNION {\n"
          + "        ?b ?p ?a\n"
          + "      }\n"
          + "      FILTER(isIRI(?a) && isIRI(?b)).\n"
          + "    } GROUP BY ?a ?b\n"
          + "  }\n"
          + "  OPTIONAL {\n"
          + "    SELECT ?a ?b (COUNT(DISTINCT ?p) as ?cio) WHERE {\n"
          + "      ?a ?p _:x .\n"
          + "      ?b ?p _:x .\n"
          + "      FILTER(isIRI(?a) && isIRI(?b)).\n"
          + "    } GROUP BY ?a ?b\n"
          + "  }\n"
          + "  OPTIONAL {\n"
          + "  \tSELECT ?a ?b (COUNT(DISTINCT ?p) as ?cii) WHERE {\n"
          + "      _:z ?p ?a .\n"
          + "      _:z ?p ?b .\n"
          + "      FILTER(isIRI(?a) && isIRI(?b)).\n"
          + "    } GROUP BY ?a ?b      \n"
          + "  }\n"
          + "}";

  private static final String SINGLE_LDSD_QUERY =
      "SELECT ((1.0 /(1.0 + COALESCE(?cd,0.0) + COALESCE(?cio,0.0) + COALESCE(?cii,0.0))) as ?ldsd) WHERE {\n"
          + "  OPTIONAL {\n"
          + "    SELECT (COUNT(DISTINCT ?p) as ?cd) WHERE {\n"
          + "      {\n"
          + "        ${a} ?p ${b}\n"
          + "      } UNION {\n"
          + "        ${b} ?p ${a}\n"
          + "      }\n"
          + "    }\n"
          + "  }\n"
          + "  OPTIONAL {\n"
          + "    SELECT (COUNT(DISTINCT ?p) as ?cio) WHERE {\n"
          + "      ${a} ?p _:x .\n"
          + "      ${b} ?p _:x .\n"
          + "    }\n"
          + "  }\n"
          + "  OPTIONAL {\n"
          + "    SELECT (COUNT(DISTINCT ?p) as ?cii) WHERE {\n"
          + "      _:z ?p ${a} .\n"
          + "      _:z ?p ${b} .\n"
          + "    }\n"
          + "  }\n"
          + "}";

  private final SPARQLService sparqlService;
  private final AllResourcesService allResourcesService;
  private final DB mapDB;

  private final HTreeMap<int[], Double> ldsdValueMap;

  @Autowired
  public LDSDWithSPARQLMetricService(
      SPARQLService sparqlService,
      AllResourcesService allResourcesService,
      @Qualifier("persistent-mapdb") DB mapDB) {
    this.sparqlService = sparqlService;
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.ldsdValueMap = mapDB.hashMap(UNWEIGHTED_LDSD_SIMILARITY_UID)
        .keySerializer(Serializer.INT_ARRAY)
        .valueSerializer(Serializer.DOUBLE).createOrOpen();
  }

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Optional<Integer> optionalResourceAKey = allResourcesService
        .getResourceKey(resourcePair.getFirst());
    if (optionalResourceAKey.isPresent()) {
      Optional<Integer> optionalResourceBKey = allResourcesService
          .getResourceKey(resourcePair.getSecond());
      if (optionalResourceBKey.isPresent()) {
        Double value = ldsdValueMap
            .get(new int[]{optionalResourceAKey.get(), optionalResourceBKey.get()});
        if (value != null) {
          return value;
        }
      }
    }
    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put("a", BlankOrIRIJsonUtil.stringForSPARQLResourceOf(resourcePair.getFirst()));
    valuesMap.put("b", BlankOrIRIJsonUtil.stringForSPARQLResourceOf(resourcePair.getSecond()));
    List<Map<String, RDFTerm>> result = sparqlService.<SelectQueryResult>query(
        new StringSubstitutor(valuesMap).replace(SINGLE_LDSD_QUERY), true).value();
    if (!result.isEmpty()) {
      return Double.parseDouble(((Literal) result.get(0).get("ldsd")).getLexicalForm());
    } else {
      return 0.0;
    }
  }

  @Override
  public void compute() {
    logger.info("Starting to computes Linked Data Semantic Distance metric.");
    List<Map<String, RDFTerm>> results = sparqlService.<SelectQueryResult>query(ALL_LDSD_QUERY,
        true).value();
    for (Map<String, RDFTerm> row : results) {
      Optional<Integer> optResAKey = allResourcesService
          .getResourceKey(new Resource((BlankNodeOrIRI) row.get("a")));
      Optional<Integer> optResBKey = allResourcesService
          .getResourceKey(new Resource((BlankNodeOrIRI) row.get("b")));
      if (optResAKey.isPresent() && optResBKey.isPresent()) {
        ldsdValueMap.put(new int[]{optResAKey.get(), optResBKey.get()},
            Double.parseDouble(((Literal) row.get("ldsd")).getLexicalForm()));
      }
    }
    mapDB.commit();
    for (Resource resourceA : allResourcesService.getResourceList()) {
      Optional<Integer> optResAKey = allResourcesService.getResourceKey(resourceA);
      for (Resource resourceB : allResourcesService.getResourceList()) {
        Optional<Integer> optResBKey = allResourcesService.getResourceKey(resourceB);
        if (optResAKey.isPresent() && optResBKey.isPresent()) {
          int[] key = new int[]{optResAKey.get(), optResBKey.get()};
          if (!ldsdValueMap.containsKey(key)) {
            ldsdValueMap.put(key, 1.0);
          }
        }
      }
    }
    mapDB.commit();
    logger.info("Linked Data Semantic Distance has been successfully computed.");
  }

}
