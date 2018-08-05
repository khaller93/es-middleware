package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.peerpressure;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.PeerPressureVertexProgram;
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
 * This class is an implementation of {@link PeerPressureClusteringMetricService} that uses the
 * {@link GremlinService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = PeerPressureClusteringMetricWithGremlinService.PEER_PRESSURE_UID)
public class PeerPressureClusteringMetricWithGremlinService implements
    PeerPressureClusteringMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(PeerPressureClusteringMetricWithGremlinService.class);

  public static final String PEER_PRESSURE_UID = "esm.service.analytics.similarity.peerpressure";
  private static final String PEER_PRESSURE_PROP_NAME = PEER_PRESSURE_UID;

  private GremlinService gremlinService;
  private PGS schema;
  private ApplicationEventPublisher eventPublisher;
  private TaskExecutor taskExecutor;

  private long lastUpdateTimestamp = 0L;
  private Lock computationLock = new ReentrantLock();

  @Autowired
  public PeerPressureClusteringMetricWithGremlinService(
      GremlinService gremlinService,
      ApplicationEventPublisher eventPublisher,
      TaskExecutor taskExecutor) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.eventPublisher = eventPublisher;
    this.taskExecutor = taskExecutor;
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
  public Boolean isSharingSameCluster(ResourcePair pair) {
    Optional<Vertex> vertexAOpt = gremlinService.traversal().V()
        .has(schema.iri().identifierAsString(),
            BlankOrIRIJsonUtil.stringValue(pair.getFirst().value())).tryNext();
    if (!vertexAOpt.isPresent()) {
      return null;
    }
    String vertexACluster = vertexAOpt.get().<String>property(PEER_PRESSURE_PROP_NAME)
        .orElse(null);
    Optional<Vertex> vertexBOpt = gremlinService.traversal().V()
        .has(schema.iri().identifierAsString(),
            BlankOrIRIJsonUtil.stringValue(pair.getSecond().value())).tryNext();
    if (!vertexBOpt.isPresent()) {
      return null;
    }
    String vertexBCluster = vertexBOpt.get().<String>property(PEER_PRESSURE_PROP_NAME)
        .orElse(null);
    if (vertexACluster == null || vertexBCluster == null) {
      return null;
    }
    return vertexACluster.equals(vertexBCluster);
  }

  @Override
  public Void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to computes peer pressure clustering metric.");
    gremlinService.lock();
    try {
      Map<Object, Object> pageRankMap = gremlinService.traversal().withComputer().V()
          .peerPressure()
          .group()
          .by(__.id())
          .by(__.values(PeerPressureVertexProgram.CLUSTER)).next();
      for (Map.Entry<Object, Object> entry : pageRankMap.entrySet()) {
        gremlinService.traversal().V(entry.getKey())
            .property(Cardinality.single, PEER_PRESSURE_PROP_NAME, entry.getValue()).iterate();
      }
      gremlinService.commit();
    } catch (Exception e) {
      gremlinService.rollback();
      throw e;
    } finally {
      gremlinService.unlock();
    }
    logger.info("Peer pressure clustering issued on {} computed on {}.", issueTimestamp,
        Instant.now());
    return null;
  }

  @Override
  public AnalysisEventStatus getStatus() {
    return null;
  }
}
