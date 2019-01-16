package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.peerpressure;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.PeerPressureVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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
  private AnalysisPipelineProcessor processor;

  @Autowired
  public PeerPressureClusteringMetricWithGremlinService(
      GremlinService gremlinService,
      AnalysisPipelineProcessor processor) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.processor = processor;
  }

  @PostConstruct
  private void setUp() {
    processor.registerAnalysisService(this, false, false, true, null);
  }

  @Override
  public Boolean isSharingSameCluster(ResourcePair pair) {
    Optional<Vertex> vertexAOpt = gremlinService.traversal().V()
        .has(schema.iri().identifierAsString(),
            BlankOrIRIJsonUtil.stringValue(pair.getFirst().value())).tryNext();
    if (!vertexAOpt.isPresent()) {
      return null;
    }
    Object vertexACluster = vertexAOpt.get().property(PEER_PRESSURE_PROP_NAME)
        .orElse(null);
    Optional<Vertex> vertexBOpt = gremlinService.traversal().V()
        .has(schema.iri().identifierAsString(),
            BlankOrIRIJsonUtil.stringValue(pair.getSecond().value())).tryNext();
    if (!vertexBOpt.isPresent()) {
      return null;
    }
    Object vertexBCluster = vertexBOpt.get().property(PEER_PRESSURE_PROP_NAME)
        .orElse(null);
    if (vertexACluster == null || vertexBCluster == null) {
      return null;
    }
    return vertexACluster.equals(vertexBCluster);
  }

  @Override
  public void compute() {
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
  }

}
