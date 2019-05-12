package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.lca;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.pairs.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.ClassHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ResourceClassService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This class implements {@link LowestCommonAncestorService}. It pre-computes the metric.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@RegisterForAnalyticalProcessing(name = LCSWithClassHierarchyService.LCS_UID, prerequisites = {
    AllResourcesService.class, ResourceClassService.class,
    ClassHierarchyService.class}, disabled = true)
public class LCSWithClassHierarchyService implements LowestCommonAncestorService {

  private static final Logger logger = LoggerFactory.getLogger(LCSWithClassHierarchyService.class);

  public static final String LCS_UID = "esm.service.analytics.dataset.lcs.hierarchy";

  private final AllResourcesService allResourcesService;
  private final ResourceClassService resourceClassService;
  private final ClassHierarchyService classHierarchyService;
  private final DB mapDB;

  private final HTreeMap<int[], Set<String>> lcsMap;

  @Autowired
  public LCSWithClassHierarchyService(
      AllResourcesService allResourcesService,
      ResourceClassService resourceClassService,
      ClassHierarchyService classHierarchyService, @Qualifier("persistent-mapdb") DB mapDB) {
    this.allResourcesService = allResourcesService;
    this.resourceClassService = resourceClassService;
    this.classHierarchyService = classHierarchyService;
    this.mapDB = mapDB;
    this.lcsMap = mapDB.hashMap(LCS_UID, Serializer.INT_ARRAY, Serializer.JAVA).createOrOpen();
  }

  @Override
  public Set<Resource> getLowestCommonAncestor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Optional<Integer> resourceAKeyOpt = allResourcesService.getResourceKey(resourcePair.getFirst());
    if (resourceAKeyOpt.isPresent()) {
      Optional<Integer> resourceBKeyOpt = allResourcesService
          .getResourceKey(resourcePair.getSecond());
      if (resourceBKeyOpt.isPresent()) {
        return lcsMap.get(new int[]{resourceAKeyOpt.get(), resourceBKeyOpt.get()}).stream()
            .map(Resource::new).collect(Collectors.toSet());
      }
    }
    return null;
  }

  @Override
  public void compute() {
    for (Resource resourceA : allResourcesService.getResourceList()) {
      Optional<Integer> resourceKeyA = allResourcesService.getResourceKey(resourceA);
      Optional<Set<Resource>> optClassesA = resourceClassService.getClassesOf(resourceA);
      if (optClassesA.isPresent() && resourceKeyA.isPresent()) {
        for (Resource resourceB : allResourcesService.getResourceList()) {
          Optional<Set<Resource>> optClassesB = resourceClassService.getClassesOf(resourceB);
          Optional<Integer> resourceKeyB = allResourcesService.getResourceKey(resourceB);
          if (optClassesB.isPresent() && resourceKeyB.isPresent()) {
            Set<Resource> commonClasses = new HashSet<>();
            for (Resource classA : classHierarchyService
                .getMostSpecificClasses(optClassesA.get())) {
              for (Resource classB : classHierarchyService
                  .getMostSpecificClasses(optClassesB.get())) {
                commonClasses.addAll(classHierarchyService.getLowestCommonAncestor(classA, classB));
              }
            }
            commonClasses = classHierarchyService.getMostSpecificClasses(commonClasses);
            lcsMap.put(new int[]{resourceKeyA.get(), resourceKeyB.get()},
                commonClasses.stream().map(Resource::getId).collect(
                    Collectors.toSet()));
          } else {
            logger.warn("Classes or key cannot be fetched for resource {}.", resourceB);
          }
        }
      } else {
        logger.warn("Classes or key cannot be fetched for resource {}.", resourceA);
      }
    }
    mapDB.commit();
  }

}
