package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.lca;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.ClassHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ResourceClassService;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link LowestCommonAncestorService} that computes the lowest common
 * ancestor using {@link ClassHierarchyService} and {@link ResourceClassService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@RegisterForAnalyticalProcessing(name = LCSWithClassHierarchyService.LCS_UID, prerequisites = {
    ClassHierarchyService.class, ResourceClassService.class})
public class LCAOnTheFlyService implements LowestCommonAncestorService {

  private final ResourceClassService resourceClassService;
  private final ClassHierarchyService classHierarchyService;

  @Autowired
  public LCAOnTheFlyService(
      ResourceClassService resourceClassService,
      ClassHierarchyService classHierarchyService) {
    this.resourceClassService = resourceClassService;
    this.classHierarchyService = classHierarchyService;
  }

  @Override
  public Set<Resource> getLowestCommonAncestor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Optional<Set<Resource>> resourceAOpt = resourceClassService
        .getClassesOf(resourcePair.getFirst());
    if (resourceAOpt.isPresent()) {
      Set<Resource> mostSpecificClassesA = classHierarchyService
          .getMostSpecificClasses(resourceAOpt.get());
      if (!mostSpecificClassesA.isEmpty()) {
        Optional<Set<Resource>> resourceBOpt = resourceClassService
            .getClassesOf(resourcePair.getSecond());
        if (resourceBOpt.isPresent()) {
          Set<Resource> mostSpecificClassesB = classHierarchyService
              .getMostSpecificClasses(resourceBOpt.get());
          if (!mostSpecificClassesB.isEmpty()) {
            if (mostSpecificClassesA.size() == 1 && mostSpecificClassesB.size() == 1) {
              return classHierarchyService
                  .getLowestCommonAncestor(mostSpecificClassesA.iterator().next(),
                      mostSpecificClassesB.iterator().next());
            } else {

            }
          }
        }
      }
    }
    return Sets.newHashSet();
  }

  @Override
  public void compute() {
    //nothing to do
  }
}
