package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.entity.SimilarityMetricKey;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.entity.SimilarityMetricResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.SimilarityMetricStoreService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is an implementation of {@link LDSDWithSPARQLMetricService} using the {@link
 * SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = LDSDWithSPARQLMetricService.UNWEIGHTED_LDSD_SIMILARITY_UID)
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

  private SPARQLService sparqlService;
  private AnalysisPipelineProcessor processor;
  private SimilarityMetricStoreService similarityMetricStoreService;

  @Autowired
  public LDSDWithSPARQLMetricService(
      SPARQLService sparqlService,
      AnalysisPipelineProcessor processor,
      SimilarityMetricStoreService similarityMetricStoreService) {
    this.sparqlService = sparqlService;
    this.processor = processor;
    this.similarityMetricStoreService = similarityMetricStoreService;
  }

  @PostConstruct
  private void setUp() {
    processor.registerAnalysisService(this, true, false, false, null);
  }

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Optional<SimilarityMetricResult> metricsResult = similarityMetricStoreService
        .findById(SimilarityMetricKey.of(UNWEIGHTED_LDSD_SIMILARITY_UID, resourcePair));
    if (metricsResult.isPresent()) {
      return metricsResult.get().getValue();
    } else {
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
  }

  @Override
  @SuppressWarnings("unchecked")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to computes Linked Data Semantic Distance metric.");
    List<Map<String, RDFTerm>> results = sparqlService.<SelectQueryResult>query(ALL_LDSD_QUERY,
        true).value();
    similarityMetricStoreService.saveAll(results.stream()
        .map(row -> new SimilarityMetricResult(SimilarityMetricKey
            .of(UNWEIGHTED_LDSD_SIMILARITY_UID, ResourcePair
                .of(new Resource((BlankNodeOrIRI) row.get("a")),
                    new Resource((BlankNodeOrIRI) row.get("b")))),
            Double.parseDouble(((Literal) row.get("ldsd")).getLexicalForm())))
        .collect(Collectors.toList()));
    logger.info("Linked Data Semantic Distance issued on {} computed on {}.", issueTimestamp,
        Instant.now());
  }

}
