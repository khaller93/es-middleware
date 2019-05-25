package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.sparql.result.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
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

  private static final int LOAD_SIZE = 10000;

  private static final String ALL_LDSD_QUERY =
      "SELECT ?a ?b ((1.0 /(1.0 + COALESCE(?cd,0.0) + COALESCE(?cio,0.0) + COALESCE(?cii,0.0))) as ?ldsd) WHERE {\n"
          + "  VALUES (?a ?b) {\n"
          + "    %s \n"
          + "  }\n"
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
        return ldsdValueMap
            .get(new int[]{optionalResourceAKey.get(), optionalResourceBKey.get()});
      }
    }
    return null;
  }

  private void processSPARQLResult(List<Map<String, RDFTerm>> results) {
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
  }

  @Override
  public void compute() {
    int n = 0;
    int total = 0;
    StringBuilder valueList = new StringBuilder();
    for (Resource resourceA : allResourcesService.getResourceList()) {
      for (Resource resourceB : allResourcesService.getResourceList()) {
        // store 0.0 for pairs with same resource.
        if (resourceA.equals(resourceB)) {
          Optional<Integer> optResAKey = allResourcesService
              .getResourceKey(resourceA);
          Optional<Integer> optResBKey = allResourcesService
              .getResourceKey(resourceB);
          if (optResAKey.isPresent() && optResBKey.isPresent()) {
            ldsdValueMap.put(new int[]{optResAKey.get(), optResBKey.get()}, 0.0);
          }
        } else { // store the result of pairs.
          valueList.append(String
              .format("(%s %s)\n", RDFTermJsonUtil.stringForSPARQLResourceOf(resourceA),
                  RDFTermJsonUtil.stringForSPARQLResourceOf(resourceB)));
          n++;
          if (n == LOAD_SIZE) {
            processSPARQLResult(sparqlService.<SelectQueryResult>query(
                String.format(ALL_LDSD_QUERY, valueList.toString()), true).value());
            total += n;
            n = 0;
            valueList = new StringBuilder();
            logger.trace("Loaded {} LDSD pairs. In total {} have been loaded.", n, total);
          }
        }
      }
    }
    if (n > 0) {
      processSPARQLResult(sparqlService.<SelectQueryResult>query(
          String.format(ALL_LDSD_QUERY, valueList.toString()), true).value());
    }
    mapDB.commit();
  }

}
