package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.event.PageRankUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.process.traversal.Operator.div;
import static org.apache.tinkerpop.gremlin.process.traversal.Operator.sum;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Column;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * This class implements methods for computing common centrality metrics.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@org.springframework.context.annotation.Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CentralityMetricsService {

  private static final Logger logger = LoggerFactory.getLogger(CentralityMetricsService.class);

  public enum METRIC {PAGERANK, DEGREE, BETWEENESS, CLOSENESS}

  private ExecutorService threadPool;
  private GremlinService gremlinService;
  private CacheManager cacheManager;
  private ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public CentralityMetricsService(GremlinService gremlinService, CacheManager cacheManager,
      ApplicationEventPublisher applicationEventPublisher) {
    this.gremlinService = gremlinService;
    this.cacheManager = cacheManager;
    this.threadPool = Executors.newCachedThreadPool();
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @EventListener
  public void onApplicationEvent(GremlinDAOReadyEvent event) {
    logger.debug("Recognized an Gremlin ready event {}.", event);
    updateMetrics();
  }

  @EventListener
  public void onApplicationEvent(GremlinDAOUpdatedEvent event) {
    logger.debug("Recognized an Gremlin update event {}.", event);
    updateMetrics();
  }

  /**
   * Updates the centrality metrics and puts them into the cache.
   */
  private void updateMetrics() {
    threadPool.submit(this::computePageRank);
    threadPool.submit(this::computeDegree);
    //threadPool.submit(this::computeBetweeness);
    //threadPool.submit(this::computeCloseness);
  }

  /**
   * Gets the value of the centrality metric {@code metric} for the given {@code resource}.
   *
   * @param metric for which the value shall be returned.
   * @param resource for which the value shall be returned.
   * @param clazz which is the type of the value.
   * @return the corresponding value, or {@code null}, if it is missing.
   */
  private <T> T getCentralityMetricValueOf(METRIC metric, Resource resource, Class<T> clazz) {
    if (resource == null) {
      throw new IllegalArgumentException("The given resource must not be null.");
    }
    Cache centralityCache = cacheManager.getCache("centrality");
    if (centralityCache == null) {
      throw new IllegalStateException("The cache for centrality metrics is not available.");
    }
    return centralityCache.get(CentralityKey.of(metric, resource), clazz);
  }

  /**
   * Returns the computed page rank for the given resource.
   *
   * @param resource for which the page rank shall be computed.
   * @return {@link Double} representing the page rank of the resource, {@code null}, if there is no
   * entry.
   */
  public Double getPageRankOf(Resource resource) {
    return getCentralityMetricValueOf(METRIC.PAGERANK, resource, Double.class);
  }

  /**
   * Computes the page rank of all resources in the maintained knowledge graph.
   */
  public void computePageRank() {
    Instant issueTimestamp = Instant.now();
    logger.info("Computes page rank metric issued on {}", issueTimestamp);
    Cache centralityCache = cacheManager.getCache("centrality");
    if (centralityCache == null) {
      throw new IllegalStateException("The cache for centrality metrics is not available.");
    }
    if (gremlinService.areTransactionsSupported()) {
      gremlinService.getTransaction().open();
    } else {
      gremlinService.getLock().lock();
    }
    try {
      GraphTraversal<Vertex, Vertex> g = gremlinService.traversal().withComputer().V().pageRank();
      Map<Object, Object> pageRankMap = g.pageRank().group().by(__.label()).by(
          __.values("gremlin.pageRankVertexProgram.pageRank")).next();
      for (Map.Entry<Object, Object> entry : pageRankMap.entrySet()) {
        centralityCache.put(CentralityKey
                .of(METRIC.PAGERANK, new Resource(BlankOrIRIJsonUtil.valueOf((String) entry.getKey()))),
            entry.getValue());
      }
    } finally {
      if (gremlinService.areTransactionsSupported()) {
        gremlinService.getTransaction().close();
      } else {
        gremlinService.getLock().unlock();
      }
    }
    applicationEventPublisher.publishEvent(new PageRankUpdatedEvent(CentralityMetricsService.this));
    logger.info("Page rank issued on {} computed on {}.", issueTimestamp, Instant.now());
  }

  /**
   * Returns the computed degree metric for the given resource.
   *
   * @param resource for which the degree metric shall be computed.
   * @return {@link Long} representing the degree metric of the resource, {@code null}, if there is
   * no entry.
   */
  public Long getDegreeOf(Resource resource) {
    return getCentralityMetricValueOf(METRIC.DEGREE, resource, Long.class);
  }

  /**
   * Computes the degree centrality for all resources using the degree of nodes. Only the incoming
   * edged are counted, and thus, this metric focus on the popularity of a node.
   */
  public void computeDegree() {
    Instant issueTimestamp = Instant.now();
    logger.info("Computes degree metric issued on {}", issueTimestamp);
    Cache centralityCache = cacheManager.getCache("centrality");
    if (centralityCache == null) {
      throw new IllegalStateException("The cache for centrality metrics is not available.");
    }
    if (gremlinService.areTransactionsSupported()) {
      gremlinService.getTransaction().open();
    } else {
      gremlinService.getLock().lock();
    }
    try {
      GraphTraversal<Vertex, Vertex> g = gremlinService.traversal().withComputer().V();
      GraphTraversal<Vertex, Map<String, Object>> resourceDegreesTraversal = g
          .project("v", "degree")
          .by(__.label()).by(__.inE().count());
      while (resourceDegreesTraversal.hasNext()) {
        Map<String, Object> node = resourceDegreesTraversal.next();
        centralityCache.put(CentralityKey
                .of(METRIC.DEGREE, new Resource(BlankOrIRIJsonUtil.valueOf((String) node.get("v")))),
            node.get("degree"));
      }
    } finally {
      if (gremlinService.areTransactionsSupported()) {
        gremlinService.getTransaction().close();
      } else {
        gremlinService.getLock().unlock();
      }
    }
    logger.info("Degree metric issued on {} computed on {}.", issueTimestamp, Instant.now());
  }

  /**
   * Returns the computed betweeness metric for the given resource.
   *
   * @param resource for which the betweeness metric shall be computed.
   * @return {@link Long} representing the betweeness metric of the resource, {@code null}, if there
   * is no entry.
   */
  public Long getBetweenessOf(Resource resource) {
    return getCentralityMetricValueOf(METRIC.BETWEENESS, resource, Long.class);
  }

  /**
   * Computes the betweeness centrality for all resources.
   */
  public void computeBetweeness() {
    Instant issueTimestamp = Instant.now();
    logger.info("Computes betweeness centrality metric issued on {}", issueTimestamp);
    Cache centralityCache = cacheManager.getCache("centrality");
    if (centralityCache == null) {
      throw new IllegalStateException("The cache for centrality metrics is not available.");
    }
    if (gremlinService.areTransactionsSupported()) {
      gremlinService.getTransaction().open();
    } else {
      gremlinService.getLock().lock();
    }
    try {
      GraphTraversal<Vertex, Map<String, Object>> traversal = gremlinService.traversal().withSack(0)
          .V()
          .store("x")
          .repeat(__.both().simplePath()).emit().path().
              group().by(__.project("a", "b").by(__.limit(Scope.local, 1)).
              by(__.tail(Scope.local, 1))).
              by(__.order().by(__.count(Scope.local))).
              select(Column.values).as("shortestPaths").
              select("x").unfold().as("v").
              select("shortestPaths").
              map(__.unfold().filter(__.unfold().where(P.eq("v"))).count()).
              sack(sum).sack().as("betweeness").
              select("v", "betweeness");

      while (traversal.hasNext()) {
        //TODO: Implement
      }
    } finally {
      if (gremlinService.areTransactionsSupported()) {
        gremlinService.getTransaction().close();
      } else {
        gremlinService.getLock().unlock();
      }
    }
    logger.info("Betweeness metric issued on {} computed on {}.", issueTimestamp, Instant.now());
  }

  /**
   * Returns the computed closeness metric for the given resource.
   *
   * @param resource for which the closeness metric shall be computed.
   * @return {@link Long} representing the closeness metric of the resource, {@code null}, if there
   * is no entry.
   */
  public Long getClosenessOf(Resource resource) {
    return getCentralityMetricValueOf(METRIC.CLOSENESS, resource, Long.class);
  }

  /**
   * Computes the closeness centrality for all resources.
   */
  public void computeCloseness() {
    Instant issueTimestamp = Instant.now();
    logger.info("Computes closeness centrality metric issued on {}", issueTimestamp);
    Cache centralityCache = cacheManager.getCache("centrality");
    if (centralityCache == null) {
      throw new IllegalStateException("The cache for centrality metrics is not available.");
    }
    if (gremlinService.areTransactionsSupported()) {
      gremlinService.getTransaction().open();
    } else {
      gremlinService.getLock().lock();
    }
    try {
      logger.debug("Computes closeness centrality metric.");
      GraphTraversal<Vertex, Map<Object, Object>> resultTraversal = gremlinService.traversal()
          .withSack(1f)
          .V().repeat(__.both().simplePath()).emit().path().
              group().by(__.project("a", "b").by(__.limit(Scope.local, 1)).
              by(__.tail(Scope.local, 1))).
              by(__.order().by(__.count(Scope.local))).
              select(Column.values).unfold().
              project("v", "length").
              by(__.limit(Scope.local, 1)).
              by(__.count(Scope.local).sack(div).sack()).
              group().by(__.select("v")).by(__.select("length").sum());
      Map<Resource, Long> closenessMap = new HashMap<>();
      while (resultTraversal.hasNext()) {
        //TODO: Implement
      }
    } finally {
      if (gremlinService.areTransactionsSupported()) {
        gremlinService.getTransaction().close();
      } else {
        gremlinService.getLock().unlock();
      }
    }
  }

  /**
   * This class represents the cache key for storing the result of the metrics.
   */
  private static class CentralityKey {

    private METRIC metric;
    private Resource resource;

    private CentralityKey(METRIC metric, Resource resource) {
      assert metric != null && resource != null;
      this.metric = metric;
      this.resource = resource;
    }

    private static CentralityKey of(METRIC metric, Resource resource) {
      return new CentralityKey(metric, resource);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CentralityKey that = (CentralityKey) o;
      return Objects.equals(metric, that.metric) &&
          Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
      return Objects.hash(metric, resource);
    }
  }

  @PreDestroy
  public void tearDown() {
    if (threadPool != null) {
      threadPool.shutdown();
    }
  }

}
