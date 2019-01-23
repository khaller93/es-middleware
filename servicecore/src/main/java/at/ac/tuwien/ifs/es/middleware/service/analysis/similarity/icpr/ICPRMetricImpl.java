package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LeastCommonSubSumersService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik.ResnikSimilarityMetricService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This class is an implementation get {@link ICPRMetricService} that uses the {@link
 * LeastCommonSubSumersService} and {@link PageRankCentralityMetricService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = "esm.service.analytics.similarity.icpr", prerequisites = {
    ResnikSimilarityMetricService.class, PageRankCentralityMetricService.class})
public class ICPRMetricImpl implements ICPRMetricService {

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    return null;
  }

  @Override
  public void compute() {

  }

}
