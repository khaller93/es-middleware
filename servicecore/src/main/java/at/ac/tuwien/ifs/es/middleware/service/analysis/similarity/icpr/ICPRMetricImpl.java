package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LeastCommonSubSumersService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This class is an implementation of {@link ICPRMetricService} that uses the {@link
 * LeastCommonSubSumersService} and {@link PageRankCentralityMetricService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = "esm.service.analytics.similarity.ldsd")
public class ICPRMetricImpl implements ICPRMetricService {

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    return null;
  }

  @Override
  public Void compute() {

    return null;
  }

  @Override
  public AnalysisEventStatus getStatus() {
    return null;
  }
}
