package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.peerpressure;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.pairs.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;

/**
 * This is a {@link AnalysisService} for computing a peer pressure algorithm on the knowledge graph.
 * Eventually, each instances is assigned to a cluster and two resources might be similar, if they
 * are located in the same cluster.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="http://tinkerpop.apache.org/docs/current/reference/#peerpressurevertexprogram">Tinkerpop
 * Peer Pressure documentation</a>
 * @since 1.0
 */
public interface PeerPressureClusteringMetricService extends AnalysisService {

  /**
   * Checks whether the resources get the given {@code pair} are in the same cluster.
   *
   * @param pair a {@link ResourcePair} for which it should be checked. It must not be null.
   * @return {@code true}, if the resources get the given {@code pair} are in the same cluster,
   * otherwise {@code false}. The returned value is {@code null}, if the given pair is unknown.
   * @throws IllegalArgumentException if the given resource pair was null.
   */
  Boolean isSharingSameCluster(ResourcePair pair);

}
