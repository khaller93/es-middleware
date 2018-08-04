package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.centrality.CentralityMetricsService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.event.InformationContentUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.event.PageRankUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * This class implements methods for computing common similarity metrics.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@org.springframework.context.annotation.Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SimilarityMetricsService {

  private static final Logger logger = LoggerFactory.getLogger(SimilarityMetricsService.class);

  private static final String LEAST_COMMON_SUBSUMER_QUERY =
      "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
          + "\n"
          + "select ?resource1 ?resource2 ?class where { \n"
          + "\t?resource1 (a/rdfs:subClassOf*) ?class.\n"
          + "    ?resource2 (a/rdfs:subClassOf*) ?class.\n"
          + "    FILTER NOT EXISTS {\n"
          + "    \t?resource1 (a/rdfs:subClassOf*) ?subClass.\n"
          + "    \t?resource2 (a/rdfs:subClassOf*) ?subClass.\n"
          + "        FILTER (?class != ?subClass) .\n"
          + "    }\n"
          + "    FILTER(?resource1 != ?resource2) .\n"
          + "}";

  public enum METRIC {DISTANCE, RESNIK, ICPR}

  private SPARQLService sparqlService;
  private GremlinService gremlinService;
  private CentralityMetricsService centralityMetricsService;
  private InformationContentService informationContentService;
  private CacheManager cacheManager;
  private TaskExecutor taskExecutor;

  @Autowired
  public SimilarityMetricsService(SPARQLService sparqlService, GremlinService gremlinService,
      CentralityMetricsService centralityMetricsService,
      InformationContentService informationContentService,
      CacheManager cacheManager, TaskExecutor taskExecutor) {
    this.sparqlService = sparqlService;
    this.gremlinService = gremlinService;
    this.centralityMetricsService = centralityMetricsService;
    this.informationContentService = informationContentService;
    this.cacheManager = cacheManager;
    this.taskExecutor = taskExecutor;
  }

  @EventListener
  public void onApplicationEvent(GremlinDAOReadyEvent event) {
    logger.debug("Recognized an Gremlin ready event {}.", event);
    taskExecutor.execute(this::computeDistance);
  }

  @EventListener
  public void onApplicationEvent(GremlinDAOUpdatedEvent event) {
    logger.debug("Recognized an Gremlin update event {}.", event);
    taskExecutor.execute(this::computeDistance);
  }

  @EventListener
  public void onApplicationEvent(PageRankUpdatedEvent event) {
    logger.debug("Recognized an Page Rank update event {}.", event);
    taskExecutor.execute(this::computeICPR);
  }

  @EventListener
  public void onApplicationEvent(InformationContentUpdatedEvent event) {
    logger.debug("Recognized an Information Content update event {}.", event);
    taskExecutor.execute(this::computeInformationContent);
  }

  /**
   * Computes the information-content between nodes.
   */
  public void computeInformationContent() {
    Instant issueTimestamp = Instant.now();
    logger.info("Resnik similarity metric issued on {}", issueTimestamp);
    Cache similarityCache = cacheManager.getCache("similarity");
    if (similarityCache == null) {
      throw new IllegalStateException("The cache for similarity metrics is not available.");
    }
    gremlinService.lock();
    try {
      Map<ResourcePair, List<Resource>> subsumers = getLeastCommonSubsumer();
      GraphTraversalSource g = gremlinService.traversal();
      g.V().as("a").V().as("b").select("a", "b").forEachRemaining(
          new Consumer<Map<String, Object>>() {
            @Override
            public void accept(Map<String, Object> vertexMap) {
              ResourcePair pair = ResourcePair
                  .of(new Resource(((Vertex) vertexMap.get("a")).label()),
                      new Resource(((Vertex) vertexMap.get("b")).label()));
              logger.trace("Went into resource pair for distance metric {}", pair);
              List<Resource> classes = subsumers.get(pair);
              if (classes != null) {
                similarityCache
                    .put(SimilarityKey.of(METRIC.RESNIK, pair), classes.stream().map(c -> {
                      Double v = informationContentService.getInformationContentOfClass(c);
                      if (v == null) {
                        v = 0.0;
                      }
                      return v;
                    }).reduce(0.0, (a, b) -> a + b));
              }
            }
          });
    } finally {
      gremlinService.unlock();
    }
    logger.info("Resnik similarity measurement issued on {} computed on {}.", issueTimestamp,
        Instant.now());
  }

  public <T> Map<ResourcePair, T> getCentralityMetricValueOf(METRIC metric,
      List<ResourcePair> resourcePairs, Class<T> clazz) {
    if (resourcePairs == null) {
      throw new IllegalArgumentException("The given pair list must not be null.");
    }
    Cache similarityCache = cacheManager.getCache("similarity");
    if (similarityCache == null) {
      throw new IllegalStateException("The cache for similarity metrics is not available.");
    }
    Map<ResourcePair, T> m = new HashMap<>();
    for (ResourcePair pair : resourcePairs) {
      m.put(pair, getCentralityMetricValueOf(metric, pair, clazz));
    }
    return m;
  }

  public <T> T getCentralityMetricValueOf(METRIC metric, ResourcePair resourcePair,
      Class<T> clazz) {
    if (resourcePair == null) {
      throw new IllegalArgumentException("The given pair must not be null.");
    }
    Cache similarityCache = cacheManager.getCache("similarity");
    if (similarityCache == null) {
      throw new IllegalStateException("The cache for similarity metrics is not available.");
    }
    return similarityCache.get(SimilarityKey.of(metric, resourcePair), clazz);
  }

  public Map<ResourcePair, Double> geICPRValuesOf(List<ResourcePair> resourcePairs) {
    return getCentralityMetricValueOf(METRIC.ICPR, resourcePairs, Double.class);
  }

  /**
   * Computes the information-content page rank between nodes.
   */
  public void computeICPR() {
    Instant issueTimestamp = Instant.now();
    logger.info("Computes distance metric issued on {}", issueTimestamp);
    Cache similarityCache = cacheManager.getCache("similarity");
    if (similarityCache == null) {
      throw new IllegalStateException("The cache for similarity metrics is not available.");
    }
    gremlinService.lock();
    Map<ResourcePair, List<Resource>> subsumers = getLeastCommonSubsumer();
    try {
      GraphTraversalSource g = gremlinService.traversal();
      g.V().as("a").V().as("b").select("a", "b").forEachRemaining(
          new Consumer<Map<String, Object>>() {
            @Override
            public void accept(Map<String, Object> vertexMap) {
              ResourcePair pair = ResourcePair
                  .of(new Resource(((Vertex) vertexMap.get("a")).label()),
                      new Resource(((Vertex) vertexMap.get("b")).label()));
              logger.trace("Went into resource pair for distance metric {}", pair);
              List<Resource> classes = subsumers.get(pair);
              if (classes != null) {
                similarityCache
                    .put(SimilarityKey.of(METRIC.ICPR, pair), classes.stream().map(c -> {
                      Double v = centralityMetricsService.getPageRankOf(c);
                      if (v == null) {
                        v = 0.0;
                      }
                      return v;
                    }).reduce(0.0, (a, b) -> a + b));
              }
            }
          });
    } finally {
      gremlinService.unlock();
    }
    logger.info("Distance similarity measurement issued on {} computed on {}.", issueTimestamp,
        Instant.now());
  }

  public Map<ResourcePair, Double> getResnikValuesOf(List<ResourcePair> resourcePairs) {
    return getCentralityMetricValueOf(METRIC.RESNIK, resourcePairs, Double.class);
  }

  /**
   * Computes the pairwise-distance between nodes.
   */
  public void computeDistance() {
    Instant issueTimestamp = Instant.now();
    logger.info("Computes distance metric issued on {}", issueTimestamp);
    Cache similarityCache = cacheManager.getCache("similarity");
    if (similarityCache == null) {
      throw new IllegalStateException("The cache for similarity metrics is not available.");
    }
    gremlinService.lock();
    try {
      GraphTraversalSource g = gremlinService.traversal();
      g.V().as("a").V().as("b").select("a", "b").forEachRemaining(
          new Consumer<Map<String, Object>>() {
            @Override
            public void accept(Map<String, Object> vertexMap) {
              ResourcePair pair = ResourcePair
                  .of(new Resource(((Vertex) vertexMap.get("a")).label()),
                      new Resource(((Vertex) vertexMap.get("b")).label()));
              GraphTraversal<Vertex, Path> distance = gremlinService.traversal().V()
                  .hasLabel(pair.getFirst().getId())
                  .until(__.hasLabel(pair.getSecond().getId()))
                  .repeat(__.both().dedup().simplePath())
                  .path()
                  .limit(1);
              //TODO: Detect non-path.
              logger.trace("Went into resource pair for distance metric {}", pair);
              if (distance.hasNext()) {
                similarityCache
                    .put(SimilarityKey.of(METRIC.DISTANCE, pair), distance.next().size() - 1);
              } else {
                similarityCache
                    .put(SimilarityKey.of(METRIC.DISTANCE, pair), Integer.MAX_VALUE);
              }
            }
          });
    } finally {
      gremlinService.unlock();
    }
    logger.info("Distance similarity measurement issued on {} computed on {}.", issueTimestamp,
        Instant.now());
  }

  /**
   * Gets the classes (least common subsummer) that are shared by two resources. It returns a map
   * with the pair and a list with common subsumer.
   */
  @Cacheable("sparql")
  public Map<ResourcePair, List<Resource>> getLeastCommonSubsumer() {
    List<Map<String, RDFTerm>> resultMap = (sparqlService.<SelectQueryResult>query(
        LEAST_COMMON_SUBSUMER_QUERY, true)).value();
    Map<ResourcePair, List<Resource>> subsumerMap = new HashMap<>();
    for (Map<String, RDFTerm> row : resultMap) {
      Resource left = new Resource((BlankNodeOrIRI) row.get("resource1"));
      Resource right = new Resource((BlankNodeOrIRI) row.get("resource2"));
      BiFunction<ResourcePair, List<Resource>, List<Resource>> comp = new BiFunction<ResourcePair, List<Resource>, List<Resource>>() {
        @Override
        public List<Resource> apply(ResourcePair pair, List<Resource> resources) {
          return pair != null ? resources : new LinkedList<>();
        }
      };
      Resource clazz = new Resource((BlankNodeOrIRI) row.get("class"));
      subsumerMap.compute(ResourcePair.of(left, right), comp).add(clazz);
      subsumerMap.compute(ResourcePair.of(right, left), comp).add(clazz);
    }
    return subsumerMap;
  }

  /**
   * Computes the distance between the first resource and second resource of a resource pair. If
   * first and second pair are the same resource, the distance will be {@code 0}.
   *
   * @param resourcePairs for which the distance shall be computed.
   * @return {@link Map} of resource pairs with the distance between given pair.
   */
  public Map<ResourcePair, Integer> getDistance(List<ResourcePair> resourcePairs) {
    Cache similarityCache = cacheManager.getCache("similarity");
    if (similarityCache == null) {
      throw new IllegalStateException("The cache for similarity is not available.");
    }
    Map<ResourcePair, Integer> distanceMap = new HashMap<>();
    for (ResourcePair pair : resourcePairs) {
      Integer distance = similarityCache.get(pair, Integer.class);
      if (distance != null) {
        distanceMap.put(pair, distance);
      }
    }
    return distanceMap;
  }

  /**
   * This class represents the cache key for storing the result of the metrics.
   */
  private static class SimilarityKey {

    private METRIC metric;
    private ResourcePair pair;

    private SimilarityKey(METRIC metric, ResourcePair pair) {
      assert metric != null && pair != null;
      this.metric = metric;
      this.pair = pair;
    }

    private static SimilarityMetricsService.SimilarityKey of(METRIC metric, ResourcePair pair) {
      return new SimilarityMetricsService.SimilarityKey(metric, pair);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SimilarityKey that = (SimilarityKey) o;
      return metric == that.metric &&
          Objects.equals(pair, that.pair);
    }

    @Override
    public int hashCode() {
      return Objects.hash(metric, pair);
    }
  }

}
