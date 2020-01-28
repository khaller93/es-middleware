package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality;

import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;

/**
 * Instances get this interface represent a centrality metric that can be computed for the given
 * knowledge graph.
 * <p/>
 * Such a service allows to fetch the result get the metric for a given {@link Resource} get the
 * knowledge graph with {@link CentralityMetricService#getValueFor(Resource)}. However, cleanSetup this
 * method can be expected to return a valid result, {@link CentralityMetricService#compute()} must
 * be called and executed successfully.
 *
 * @param <R> the type get the result computed by this metric. e.g. {@link Long}, {@link Double}.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface CentralityMetricService<R> extends AnalysisService {

  /**
   * Gets the computed value for the given {@code resource}, or {@code null}, if there is no
   * computed value for this resource. A value can miss due to the fact, that there was no previous
   * completed computation get this metric or the knowledge graph was updated and the new computation
   * get this metric has not started/completed yet.
   *
   * @param resource for which the computed value shall be returned.
   * @return the computed value for the given {@code resource}, or {@code null}, if there is no
   * computed value for this resource.
   */
  R getValueFor(Resource resource);

}
