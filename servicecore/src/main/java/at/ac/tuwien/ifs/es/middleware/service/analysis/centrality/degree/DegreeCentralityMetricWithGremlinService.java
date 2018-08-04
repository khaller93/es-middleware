package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link DegreeCentralityMetricService} that uses the {@link
 * at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService} to compute the
 * degree of resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = "esm.service.analytics.centrality.degree")
public class DegreeCentralityMetricWithGremlinService implements DegreeCentralityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(DegreeCentralityMetricWithGremlinService.class);

  public static final String DEGREE_PROP_NAME = "esm.service.analytics.centrality.degree";

  private GremlinService gremlinService;
  private PGS schema;
  private TaskExecutor taskExecutor;
  private ApplicationEventPublisher eventPublisher;

  private long lastUpdateTimestamp = 0L;
  private final Lock computationLock = new ReentrantLock();

  @Autowired
  public DegreeCentralityMetricWithGremlinService(GremlinService gremlinService,
      ApplicationEventPublisher eventPublisher, TaskExecutor taskExecutor) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.taskExecutor = taskExecutor;
    this.eventPublisher = eventPublisher;
  }

  @EventListener
  public void onApplicationEvent(GremlinDAOReadyEvent event) {
    logger.debug("Recognized an Gremlin ready event {}.", event);
    startComputation(event.getTimestamp());
  }

  @EventListener
  public void onApplicationEvent(GremlinDAOUpdatedEvent event) {
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
  public Long getValueFor(Resource resource) {
    String resourceIRI = BlankOrIRIJsonUtil.stringValue(resource.value());
    GraphTraversal<Vertex, Vertex> traversal = gremlinService.traversal().V()
        .has(schema.iri().identifierAsString(), resourceIRI);
    if (!traversal.hasNext()) {
      return null;
    }
    return (Long) traversal.next().property(DEGREE_PROP_NAME)
        .orElse(gremlinService.traversal().V().has(schema.iri().identifierAsString(), resourceIRI)
            .outE().count().tryNext().orElse(null));
  }

  @Override
  public Void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to computes degree metric.");
    gremlinService.lock();
    try {
      Map<Object, Object> degreeMap = gremlinService
          .traversal().V().group().by(__.id()).by(__.inE().count()).next();
      for (Map.Entry<Object, Object> entry : degreeMap.entrySet()) {
        gremlinService.traversal().V(entry.getKey())
            .property(Cardinality.single, DEGREE_PROP_NAME, entry.getValue()).iterate();
      }
      gremlinService.commit();
    } catch (Exception e) {
      gremlinService.rollback();
      throw e;
    } finally {
      gremlinService.unlock();
    }
    logger.info("Degree metric issued on {} computed on {}.", issueTimestamp, Instant.now());
    return null;
  }

  @Override
  public AnalysisEventStatus getStatus() {
    return null;
  }
}
