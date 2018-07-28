package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.event.InformationContentUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.event.PageRankUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * This service provides information content services for the maintained knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class InformationContentService {

  private static final Logger logger = LoggerFactory.getLogger(InformationContentService.class);

  private GremlinService gremlinService;
  private CacheManager cacheManager;
  private ExecutorService threadPool;
  private ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public InformationContentService(GremlinService gremlinService,
      ApplicationEventPublisher applicationEventPublisher, CacheManager cacheManager) {
    this.gremlinService = gremlinService;
    this.cacheManager = cacheManager;
    this.applicationEventPublisher = applicationEventPublisher;
    this.threadPool = Executors.newCachedThreadPool();
  }

  @EventListener
  public void onApplicationEvent(GremlinDAOReadyEvent event) {
    logger.debug("Recognized an Gremlin ready event {}.", event);
    updateMetrics();
  }

  @EventListener
  @CacheEvict(value = "information-content", allEntries = true)
  public void onApplicationEvent(GremlinDAOUpdatedEvent event) {
    logger.debug("Recognized an Gremlin update event {}.", event);
    updateMetrics();
  }

  /**
   * Updates the information content for classes.
   */
  private void updateMetrics() {
    threadPool.submit(this::computeInformationContentForClasses);
  }

  /**
   * Gets all the classes in the knowledge graph. Classes are those, which have a {@code rdf:type}
   * relationship with {@code rdfs:Class} and/or have an incoming {@code rdf:type} relationship.
   *
   * @return all the classes in the knowledge graph.
   */
  @Cacheable("gremlin")
  @SuppressWarnings("unchecked")
  public List<Resource> getAllClasses() {
    return gremlinService.traversal().V()
        .union(__.inE("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").inV(),
            __.as("c").out("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
                .hasLabel("http://www.w3.org/2000/01/rdf-schema#Class").select("c")).dedup()
        .toList()
        .stream().map(v -> new Resource(BlankOrIRIJsonUtil.valueOf(v.label())))
        .collect(Collectors.toList());
  }

  /**
   * Gets the information content of the given {@code clazz}. It can be {@code null}, if given
   * resource is no class or it has not already been computed.
   *
   * @param clazz for which the information content shall be computed.
   * @return information content of the given {@code clazz}. It can be {@code null}, if given *
   * resource is no class or it has not already been computed.
   */
  public Double getInformationContentOfClass(Resource clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("The given class resource must not be null.");
    }
    Cache icCache = cacheManager.getCache("information-content");
    if (icCache == null) {
      throw new IllegalStateException("The cache for information-content is not available.");
    }
    return icCache.get(clazz, Double.class);
  }

  /**
   * Gets a map of all classes in the knowledge graph with their information content. It takes the
   * classes resulting from {@link InformationContentService#getAllClasses()} and computes the
   * number of all unique members  as well as the number per class. The graph query also takes
   * {@code rdfs:subClassOf} relationships into consideration (class hierarchies).
   * <p/>
   * Then the information content is computed with the formula {@code -log(#class_instances/total)}
   * for each class. The result is then returned in a map.
   * <p/>
   * There are possibly classes with no instances, and to avoid problems a la place correction is
   * applied.
   */
  public void computeInformationContentForClasses() {
    Instant issueTimestamp = Instant.now();
    logger.info("Computes information content metric issued on {}", issueTimestamp);
    Cache icCache = cacheManager.getCache("information-content");
    if (icCache == null) {
      throw new IllegalStateException("The cache for centrality metrics is not available.");
    }
    gremlinService.lock();
    try {
      List<Resource> allClasses = getAllClasses();
      if (!allClasses.isEmpty()) {
        GraphTraversalSource g = gremlinService.traversal();
        /* total number */
        Long total = g.V().dedup().count().next();
        /* number per class */
        String[] addClassLabels = allClasses.stream().skip(1).map(Resource::getId)
            .toArray(String[]::new);
        Map<Object, Object> classInstancesMap = g
            .V().hasLabel(allClasses.get(0).getId(), addClassLabels).until(
                __.or(__.not(__.in("http://www.w3.org/2000/01/rdf-schema#subClassOf")),
                    __.cyclicPath()))
            .repeat(__.in("http://www.w3.org/2000/01/rdf-schema#subClassOf")).group().by(__.label())
            .by(__.in("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").dedup().count()).next();
        Map<Resource, Double> icClassMap = classInstancesMap.entrySet().stream().collect(
            Collectors.toMap(e -> new Resource(BlankOrIRIJsonUtil.valueOf((String) e.getKey())),
                e -> {
                  Double p = (((((Long) e.getValue()).doubleValue()) + 1.0) / total);
                  return -Math.log(p);
                }));
        for (Resource clazz : allClasses) {
          icCache.put(clazz, icClassMap.getOrDefault(clazz, -Math.log(1.0 / total)));
        }
        logger.trace("The information content for {} is {}", allClasses,
            allClasses.stream().map(r -> icCache.get(r, Double.class))
                .collect(Collectors.toList()));
      }
    } finally {
      gremlinService.unlock();
    }
    applicationEventPublisher
        .publishEvent(new InformationContentUpdatedEvent(InformationContentService.this));
    logger.info("Information Content issued on {} computed on {}.", issueTimestamp, Instant.now());
  }

}
