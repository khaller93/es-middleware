package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.common.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.lca.LowestCommonAncestorService;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link LCAPRMetricService} that computes the LCAPR metric on the
 * fly.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = "esm.service.analytics.similarity.icpr.onthefly",
    prerequisites = {LowestCommonAncestorService.class, PageRankCentralityMetricService.class})
public class LCAPRMetricOnTheFlyService implements LCAPRMetricService {

  private final LowestCommonAncestorService lowestCommonAncestorService;
  private final PageRankCentralityMetricService pageRankCentralityMetricService;

  @Autowired
  public LCAPRMetricOnTheFlyService(
      LowestCommonAncestorService lowestCommonAncestorService,
      PageRankCentralityMetricService pageRankCentralityMetricService) {
    this.lowestCommonAncestorService = lowestCommonAncestorService;
    this.pageRankCentralityMetricService = pageRankCentralityMetricService;
  }

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not nbe null.");
    Set<Resource> lowestCommonAncestor = lowestCommonAncestorService
        .getLowestCommonAncestor(resourcePair);
    if (lowestCommonAncestor != null && !lowestCommonAncestor.isEmpty()) {
      Double value = lowestCommonAncestor.stream()
          .map(pageRankCentralityMetricService::getValueFor)
          .filter(Objects::nonNull).reduce(0.0, (a, b) -> a + b);
      return value / lowestCommonAncestor.size();
    }
    return 0.0;
  }

  @Override
  public void compute() {
    //nothing to do
  }
}
