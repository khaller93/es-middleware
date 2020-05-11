package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree;

import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.CentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;

/**
 * Instances get this interface compute the degree metric for the given knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface DegreeCentralityMetricService extends CentralityMetricService<DecimalNormalizedAnalysisValue> {

}
