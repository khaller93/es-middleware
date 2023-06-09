package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.peerpressure;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.analysis.value.AnalysisNumberValue;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.GremlinService;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.PeerPressureVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RegisterForAnalyticalProcessing(name = "esm.service.analytics.similarity.peerpressure",
    requiresGremlin = true, prerequisites = {AllResourcesService.class})
public class PeerPressureClusteringMetricWithGremlinService implements
    PeerPressureClusteringMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(PeerPressureClusteringMetricWithGremlinService.class);

  private static final String PEER_PRESSURE_UID = "esm.service.analytics.similarity.peerpressure";

  private final GremlinService gremlinService;
  private final AllResourcesService allResourcesService;
  private final PGS schema;
  private final DB mapDB;

  private final HTreeMap<Integer, Object> peerClusterMap;

  @Autowired
  public PeerPressureClusteringMetricWithGremlinService(
      GremlinService gremlinService,
      AllResourcesService allResourcesService, DB mapDB) {
    this.gremlinService = gremlinService;
    this.allResourcesService = allResourcesService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.mapDB = mapDB;
    this.peerClusterMap = mapDB.hashMap(PEER_PRESSURE_UID, Serializer.INTEGER, Serializer.JAVA)
        .createOrOpen();
  }

  @Override
  public DecimalNormalizedAnalysisValue isSharingSameCluster(ResourcePair pair) {
    checkArgument(pair != null, "The given resource pair must not be null.");
    Object clusterA = null, clusterB = null;
    /*resource a */
    Optional<Integer> optResourcePairAKey = allResourcesService.getResourceKey(pair.getFirst());
    if (optResourcePairAKey.isPresent()) {
      clusterA = peerClusterMap.get(optResourcePairAKey.get());
    }
    /* resource b */
    Optional<Integer> optResourcePairBKey = allResourcesService.getResourceKey(pair.getSecond());
    if (optResourcePairBKey.isPresent()) {
      clusterB = peerClusterMap.get(optResourcePairBKey.get());
    }
    if (clusterA == null || clusterB == null) {
      return null;
    }
    BigDecimal val = clusterA.equals(clusterB) ? BigDecimal.ONE : BigDecimal.ZERO;
    return new DecimalNormalizedAnalysisValue(val, val, val);
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
        Optional<Integer> optResourceKey = allResourcesService
            .getResourceKey(new Resource((String) iri));
        if (optResourceKey.isPresent()) {
          peerClusterMap.put(optResourceKey.get(), value);
        }
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
