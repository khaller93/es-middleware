package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.lca.LowestCommonAncestorService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation get {@link ResnikSimilarityMetricService} which computes the resnik
 * similarity on the fly for a resource pair.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = ResnikSimilarityOnTheFlyService.UID,
    prerequisites = {ClassEntropyService.class, LowestCommonAncestorService.class}, disabled = true)
public class ResnikSimilarityOnTheFlyService implements ResnikSimilarityMetricService {

  private final LowestCommonAncestorService lowestCommonAncestorService;
  private final ClassEntropyService classEntropyService;

  public static final String UID = "esm.service.analysis.sim.resnik.onthefly";

  @Autowired
  public ResnikSimilarityOnTheFlyService(
      LowestCommonAncestorService lowestCommonAncestorService,
      ClassEntropyService classEntropyService) {
    this.lowestCommonAncestorService = lowestCommonAncestorService;
    this.classEntropyService = classEntropyService;
  }

  @Override
  public DecimalNormalizedAnalysisValue getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Set<Resource> lowestCommonAncestors = lowestCommonAncestorService
        .getLowestCommonAncestor(resourcePair);
    if (lowestCommonAncestors != null) {
      if (!lowestCommonAncestors.isEmpty()) {
        BigDecimal value = lowestCommonAncestors.stream()
            .map(classEntropyService::getEntropyForClass)
            .filter(Objects::nonNull).map(DecimalNormalizedAnalysisValue::getValue)
            .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        return new DecimalNormalizedAnalysisValue(value, value, null);
      } else {
        return new DecimalNormalizedAnalysisValue(BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO);
      }
    }
    return null;
  }

  @Override
  public void compute() {
    //nothing to do
  }
}
