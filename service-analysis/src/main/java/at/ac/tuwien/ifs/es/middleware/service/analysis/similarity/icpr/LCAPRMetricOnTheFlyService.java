package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.CentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.lca.LowestCommonAncestorService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import java.math.BigDecimal;
import java.math.MathContext;
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
@Service
@RegisterForAnalyticalProcessing(name = "esm.service.analytics.similarity.icpr.onthefly",
    prerequisites = {LowestCommonAncestorService.class,
        PageRankCentralityMetricService.class}, disabled = true)
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
  public DecimalNormalizedAnalysisValue getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not nbe null.");
    Set<Resource> lowestCommonAncestor = lowestCommonAncestorService
        .getLowestCommonAncestor(resourcePair);
    if (lowestCommonAncestor != null && !lowestCommonAncestor.isEmpty()) {
      BigDecimal totalSum = BigDecimal.valueOf(0);
      for (Resource r : lowestCommonAncestor) {
        DecimalNormalizedAnalysisValue value = pageRankCentralityMetricService
            .getValueFor(r);
        if (value != null) {
          BigDecimal decimalValue = value.getValue();
          if (decimalValue != null) {
            totalSum = totalSum.add(decimalValue);
          }
        }
      }
      return new DecimalNormalizedAnalysisValue(
          totalSum.divide(BigDecimal.valueOf(lowestCommonAncestor.size()), MathContext.DECIMAL64),
          null, null);
    }
    return new DecimalNormalizedAnalysisValue(BigDecimal.ZERO);
  }

  @Override
  public void compute() {
    //nothing to do
  }
}
