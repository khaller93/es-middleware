package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.lca;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.ClassHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ResourceClassService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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

  private final HTreeMap<int[], int[]> lcsMap;

  @Autowired
  public LCSWithClassHierarchyService(
      AllResourcesService allResourcesService,
      ResourceClassService resourceClassService,
      ClassHierarchyService classHierarchyService, DB mapDB) {
    this.allResourcesService = allResourcesService;
    this.resourceClassService = resourceClassService;
    this.classHierarchyService = classHierarchyService;
    this.mapDB = mapDB;
    this.lcsMap = mapDB.hashMap(LCS_UID, Serializer.INT_ARRAY, Serializer.INT_ARRAY).createOrOpen();
  }

  @Override
  public Set<Resource> getLowestCommonAncestor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Optional<Integer> resourceAKeyOpt = allResourcesService.getResourceKey(resourcePair.getFirst());
    if (resourceAKeyOpt.isPresent()) {
      Optional<Integer> resourceBKeyOpt = allResourcesService
          .getResourceKey(resourcePair.getSecond());
      if (resourceBKeyOpt.isPresent()) {
        Set<Resource> lowestCommonAncestors = new HashSet<>();
        int[] lcaIds = lcsMap.get(new int[]{resourceAKeyOpt.get(), resourceBKeyOpt.get()});
        if (lcaIds != null) {
          for (int lcaId : lcaIds) {
            allResourcesService.getResourceIdFor(lcaId).ifPresent(iri -> {
              lowestCommonAncestors.add(new Resource(iri));
            });
          }
          return lowestCommonAncestors;
        }
      }
    }
    return null;
  }

  private Set<Resource> computeOrGetMostSpecificClassesFromCache(
      Map<ResourceListKey, Set<Resource>> cache, Set<Resource> key) {
    ResourceListKey k = new ResourceListKey(key);
    Set<Resource> values = cache.get(k);
    if (values == null) {
      values = classHierarchyService.getMostSpecificClasses(key);
      cache.put(k, values);
    }
    return values;
  }

  private Set<Resource> getCommonClasses(
      Table<ResourceListKey, ResourceListKey, Set<Resource>> cache,
      Map<ResourceListKey, Set<Resource>> specificClassesMap,
      Set<Resource> classesA, Set<Resource> classesB) {
    ResourceListKey keyA = new ResourceListKey(classesA);
    ResourceListKey keyB = new ResourceListKey(classesB);
    if (cache.contains(keyA, keyB)) {
      return cache.get(keyA, keyB);
    } else if (cache.contains(keyB, keyA)) {
      return cache.get(keyB, keyA);
    } else {
      Set<Resource> commonClasses = new HashSet<>();
      for (Resource classA : computeOrGetMostSpecificClassesFromCache(specificClassesMap,
          classesA)) {
        for (Resource classB : computeOrGetMostSpecificClassesFromCache(specificClassesMap,
            classesB)) {
          commonClasses.addAll(classHierarchyService.getLowestCommonAncestor(classA, classB));
        }
      }
      commonClasses = classHierarchyService.getMostSpecificClasses(commonClasses);
      cache.put(keyA, keyB, new HashSet<>(commonClasses));
      return commonClasses;
    }
  }

  @Override
  public void compute() {
    int n = 0;
    Map<int[], int[]> storageCache = new HashMap<>();
    for (Resource resourceA : allResourcesService.getResourceList()) {
      Optional<Integer> resourceKeyA = allResourcesService.getResourceKey(resourceA);
      Optional<Set<Resource>> optClassesA = resourceClassService.getClassesOf(resourceA);
      if (optClassesA.isPresent() && resourceKeyA.isPresent()) {
        for (Resource resourceB : allResourcesService.getResourceList()) {
          Optional<Set<Resource>> optClassesB = resourceClassService.getClassesOf(resourceB);
          Optional<Integer> resourceKeyB = allResourcesService.getResourceKey(resourceB);
          if (optClassesB.isPresent() && resourceKeyB.isPresent()) {
            List<Integer> lowestCommonAncestor = classHierarchyService
                .getLowestCommonAncestor(optClassesA.get(), optClassesB.get()).stream()
                .map(allResourcesService::getResourceKey).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
            int[] lcas = new int[lowestCommonAncestor.size()];
            for (int i = 0; i < lowestCommonAncestor.size(); i++) {
              lcas[i] = lowestCommonAncestor.get(i);
            }
            storageCache.put(new int[]{resourceKeyA.get(), resourceKeyB.get()}, lcas);
          } else {
            logger.warn("Classes or key cannot be fetched for resource {}.", resourceB);
          }
          n++;
          if (n % 100000 == 0) {
            logger.trace("Processed LCS for {} pairs.", n);
            lcsMap.putAll(storageCache);
            storageCache.clear();
          }
        }
      } else {
        logger.warn("Classes or key cannot be fetched for resource {}.", resourceA);
      }
    }
    if (!storageCache.isEmpty()) {
      lcsMap.putAll(storageCache);
    }
    mapDB.commit();
  }

}
