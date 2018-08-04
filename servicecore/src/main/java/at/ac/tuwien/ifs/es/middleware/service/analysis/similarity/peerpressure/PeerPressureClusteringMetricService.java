package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.peerpressure;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;

/**
 * This is a {@link AnalysisService} for computing a peer pressure algorithm on the knowledge graph.
 * Eventually, each instances is assigned to a cluster and two resources might be similar, if they
 * are located in the same cluster.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface PeerPressureClusteringMetricService extends AnalysisService<Void> {

  /**
   * Checks whether the resources of the given {@code pair} are in the same cluster.
   *
   * @return {@code true}, if the resources of the given {@code pair} are in the same cluster,
   * otherwise {@code false}. The returned value is {@code null}, if the given pair is unknown.
   */
  Boolean isSharingSameCluster(ResourcePair pair);

}
