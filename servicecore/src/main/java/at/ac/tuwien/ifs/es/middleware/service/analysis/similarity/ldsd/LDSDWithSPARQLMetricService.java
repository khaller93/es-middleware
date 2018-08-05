package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.SimilarityKey;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

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
  private ApplicationEventPublisher eventPublisher;
  private TaskExecutor taskExecutor;
  private Cache similarityCache;

  private long lastUpdateTimestamp = 0L;
  private Lock computationLock = new ReentrantLock();

  @Autowired
  public LDSDWithSPARQLMetricService(
      SPARQLService sparqlService,
      ApplicationEventPublisher eventPublisher,
      TaskExecutor taskExecutor,
      CacheManager cacheManager) {
    this.sparqlService = sparqlService;
    this.eventPublisher = eventPublisher;
    this.taskExecutor = taskExecutor;
    this.similarityCache = cacheManager.getCache("similarity");
  }

  @EventListener
  public void onApplicationEvent(SPARQLDAOReadyEvent event) {
    logger.debug("Recognized an SPARQL ready event {}.", event);
    startComputation(event.getTimestamp());
  }

  @EventListener
  public void onApplicationEvent(SPARQLDAOUpdatedEvent event) {
    logger.debug("Recognized an Gremlin update event {}.", event);
    startComputation(event.getTimestamp());
  }

  private void startComputation(long eventTimestamp) {
    computationLock.lock();
    try {
      if (lastUpdateTimestamp < eventTimestamp) {
        taskExecutor.execute(this::compute);
        lastUpdateTimestamp = eventTimestamp;
      }
    } finally {
      computationLock.unlock();
    }
  }

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    Double ldsdValue = similarityCache
        .get(SimilarityKey.of(UNWEIGHTED_LDSD_SIMILARITY_UID, resourcePair), Double.class);
    if (ldsdValue != null) {
      return ldsdValue;
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
  public Void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to computes Linked Data Semantic Distance metric.");
    if (similarityCache != null) {
      List<Map<String, RDFTerm>> results = sparqlService.<SelectQueryResult>query(ALL_LDSD_QUERY,
          true).value();
      for (Map<String, RDFTerm> row : results) {
        Resource resourceA = new Resource((BlankNodeOrIRI) row.get("a"));
        Resource resourceB = new Resource((BlankNodeOrIRI) row.get("b"));
        similarityCache.put(SimilarityKey
                .of(UNWEIGHTED_LDSD_SIMILARITY_UID, ResourcePair.of(resourceA, resourceB)),
            Double.parseDouble(((Literal) row.get("ldsd")).getLexicalForm()));
      }
      logger.info("Linked Data Semantic Distance issued on {} computed on {}.", issueTimestamp,
          Instant.now());
    } else {
      logger.warn("Linked Data Semantic Distance was skipped, because cache is missing.");
    }
    return null;
  }

  @Override
  public AnalysisEventStatus getStatus() {
    return null;
  }

}
