package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.peerpressure;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.MapDB;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.PeerPressureVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


/**
 * This class is an implementation get {@link PeerPressureClusteringMetricService} that uses the
 * {@link GremlinService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = "esm.service.analytics.similarity.peerpressure")
public class PeerPressureClusteringMetricWithGremlinService implements
    PeerPressureClusteringMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(PeerPressureClusteringMetricWithGremlinService.class);

  private static final String PEER_PRESSURE_UID = "esm.service.analytics.similarity.peerpressure";

  private final GremlinService gremlinService;
  private final PGS schema;
  private final DB mapDB;
  private final AnalysisPipelineProcessor processor;

  private final HTreeMap<String, Long> peerClusterMap;

  @Autowired
  public PeerPressureClusteringMetricWithGremlinService(
      GremlinService gremlinService,
      DB mapDB,
      AnalysisPipelineProcessor processor) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.mapDB = mapDB;
    this.peerClusterMap = mapDB.hashMap(PEER_PRESSURE_UID, Serializer.STRING, Serializer.LONG)
        .createOrOpen();
    this.processor = processor;
  }

  @PostConstruct
  private void setUp() {
    processor.registerAnalysisService(this, false, false, true, null);
  }

  @Override
  public Boolean isSharingSameCluster(ResourcePair pair) {
    Long clusterA = peerClusterMap.get(pair.getFirst().getId());
    Long clusterB = peerClusterMap.get(pair.getSecond().getId());
    if (clusterA == null || clusterB == null) {
      return null;
    }
    return clusterA.equals(clusterB);
  }

  @Override
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to computes peer pressure clustering metric.");
    gremlinService.lock();
    try {
      gremlinService.traversal().withComputer().V()
          .peerPressure()
          .group()
          .by(__.map(traverser -> schema.iri().<String>apply((Element) traverser.get())))
          .by(__.values(PeerPressureVertexProgram.CLUSTER)).next().forEach((iri, value) -> {
        logger.debug(">>>> {} <<<<< {}", iri.getClass().getSimpleName(),
            value.getClass().getSimpleName());
        //peerClusterMap.put((String) iri, (Long) value);
      });
      mapDB.commit();
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
