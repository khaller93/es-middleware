package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LeastCommonSubSumersService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.SimilarityKey;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BinaryOperator;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link ResnikSimilarityMetricService} which tries to pre-compute the
 * values for all resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = ResnikSimilarityMetricServiceImpl.RESNIK_SIMILARITY_UID)
public class ResnikSimilarityMetricServiceImpl implements ResnikSimilarityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(ResnikSimilarityMetricServiceImpl.class);

  public static final String RESNIK_SIMILARITY_UID = "esm.service.analysis.sim.resnik";

  private GremlinService gremlinService;
  private PGS schema;
  private ClassEntropyService classEntropyService;
  private LeastCommonSubSumersService leastCommonSubSumersService;
  private Cache similarityCache;
  private boolean similarityCacheAvailable;

  @Autowired
  public ResnikSimilarityMetricServiceImpl(
      GremlinService gremlinService,
      ClassEntropyService classEntropyService,
      LeastCommonSubSumersService leastCommonSubSumersService,
      CacheManager cacheManager) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.classEntropyService = classEntropyService;
    this.leastCommonSubSumersService = leastCommonSubSumersService;
    this.similarityCache = cacheManager.getCache("similarity");
    this.similarityCacheAvailable = similarityCache != null;
  }

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    Double simValue = similarityCache
        .get(SimilarityKey.of(RESNIK_SIMILARITY_UID, resourcePair), Double.class);
    if (simValue != null) {
      return simValue;
    } else {
      if (!gremlinService.traversal().V().has(schema.iri().identifierAsString(),
          BlankOrIRIJsonUtil.stringValue(resourcePair.getFirst().value())).hasNext()
          || !gremlinService.traversal().V().has(schema.iri().identifierAsString(),
          BlankOrIRIJsonUtil.stringValue(resourcePair.getSecond().value())).hasNext()) {
        return null;
      } else {
        return computeIC(resourcePair);
      }
    }
  }

  private Double computeIC(ResourcePair pair) {
    Set<Resource> classes = leastCommonSubSumersService.getLeastCommonSubSumersFor(pair);
    return classes.stream().map(clazz -> classEntropyService.getEntropyForClass(clazz))
        .reduce(0.0, BinaryOperator.maxBy(Double::compareTo));
  }

  @Override
  public Void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Started to compute the Resnik similarity metric.");
    gremlinService.lock();
    try {
      Iterator<Vertex> verticesAIterator = gremlinService.traversal().getGraph().vertices();
      while (verticesAIterator.hasNext()) {
        Vertex vertexA = verticesAIterator.next();
        Iterator<Vertex> verticesBIterator = gremlinService.traversal().getGraph().vertices();
        while (verticesBIterator.hasNext()) {
          Vertex vertexB = verticesBIterator.next();
          ResourcePair pair = ResourcePair.of(new Resource(schema.iri().<String>apply(vertexA)),
              new Resource(schema.iri().<String>apply(vertexB)));
          Double clazzSumIC = computeIC(pair);
          if (clazzSumIC != null && similarityCacheAvailable) {
            similarityCache.put(SimilarityKey.of(RESNIK_SIMILARITY_UID, pair), clazzSumIC);
          }
        }
      }
    } finally {
      gremlinService.unlock();
    }
    logger.info("Resnik similarity measurement issued on {} computed on {}.", issueTimestamp,
        Instant.now());
    return null;
  }

  @Override
  public AnalysisEventStatus getStatus() {
    return null;
  }
}
