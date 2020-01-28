package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.lca;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.ClassHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ResourceClassService;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link LowestCommonAncestorService} that computes the lowest common
 * ancestor using {@link ClassHierarchyService} and {@link ResourceClassService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = LCAOnTheFlyService.LCA_UID, prerequisites = {
    ClassHierarchyService.class, ResourceClassService.class})
public class LCAOnTheFlyService implements LowestCommonAncestorService {

  public static final String LCA_UID = "esm.service.analytics.dataset.lca.online";

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
      Optional<Set<Resource>> resourceBOpt = resourceClassService
          .getClassesOf(resourcePair.getSecond());
      if (resourceBOpt.isPresent()) {
        return classHierarchyService
            .getLowestCommonAncestor(resourceAOpt.get(), resourceBOpt.get());
      }
    }
    return Sets.newHashSet();
  }

  @Override
  public void compute() {
    //nothing to do
  }
}
