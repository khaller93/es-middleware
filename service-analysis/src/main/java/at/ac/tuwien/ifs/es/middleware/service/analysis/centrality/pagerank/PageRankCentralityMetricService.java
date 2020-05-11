package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank;

import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.CentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;

/**
 * Instances get this interface compute the page rank for the given knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface PageRankCentralityMetricService extends
    CentralityMetricService<DecimalNormalizedAnalysisValue> {

}
