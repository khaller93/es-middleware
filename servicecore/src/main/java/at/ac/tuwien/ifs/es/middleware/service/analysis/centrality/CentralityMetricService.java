package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;

/**
 * Instances of this interface represent a centrality metric that can be computed for the given
 * knowledge graph.
 * <p/>
 * Such a service allows to fetch the result of the metric for a given {@link Resource} of the
 * knowledge graph with {@link CentralityMetricService#getValueFor(Resource)}. However, before this
 * method can be expected to return a valid result, {@link CentralityMetricService#compute()} must
 * be called and executed successfully.
 *
 * @param <R> the type of the result computed by this metric. e.g. {@link Long}, {@link Double}.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface CentralityMetricService<R> extends AnalysisService<Void> {

  /**
   * Gets the computed value for the given {@code resource}, or {@code null}, if there is no
   * computed value for this resource. A value can miss due to the fact, that there was no previous
   * completed computation of this metric or the knowledge graph was updated and the new computation
   * of this metric has not started/completed yet.
   *
   * @param resource for which the computed value shall be returned.
   * @return the computed value for the given {@code resource}, or {@code null}, if there is no
   * computed value for this resource.
   */
  R getValueFor(Resource resource);

}
