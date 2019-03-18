package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.lca.LowestCommonAncestorService;
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
    prerequisites = {ClassEntropyService.class, LowestCommonAncestorService.class})
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
  public Double getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Set<Resource> lowestCommonAncestors = lowestCommonAncestorService
        .getLowestCommonAncestor(resourcePair);
    if (lowestCommonAncestors != null) {
      if (!lowestCommonAncestors.isEmpty()) {
        double resnikValue = lowestCommonAncestors.stream()
            .map(classEntropyService::getEntropyForClass)
            .filter(Objects::nonNull).reduce(0.0, (a, b) -> a + b);
        return resnikValue / lowestCommonAncestors.size();
      } else {
        return 0.0;
      }
    }
    return null;
  }

  @Override
  public void compute() {
    //nothing to do
  }
}
