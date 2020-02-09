package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.lca.LowestCommonAncestorService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This class is an implementation of {@link LCAPRMetricService} that pre-computes this metric using
 * the {@link LowestCommonAncestorService} and {@link PageRankCentralityMetricService}. The
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@RegisterForAnalyticalProcessing(name = "esm.service.analytics.similarity.icpr", prerequisites = {
    LowestCommonAncestorService.class, PageRankCentralityMetricService.class}, disabled = true)
public class LCAPRMetricImpl implements LCAPRMetricService {

  private static final Logger logger = LoggerFactory.getLogger(LCAPRMetricImpl.class);

  public static final String UID = "esm.service.analysis.sim.lcapr";

  private final AllResourcesService allResourcesService;
  private final LowestCommonAncestorService lowestCommonAncestorService;
  private final PageRankCentralityMetricService pageRankCentralityMetricService;

  private final DB mapDB;
  private final HTreeMap<int[], Double> lcaprValueMap;

  @Autowired
  public LCAPRMetricImpl(
      AllResourcesService allResourcesService,
      LowestCommonAncestorService lowestCommonAncestorService,
      PageRankCentralityMetricService pageRankCentralityMetricService, DB mapDB) {
    this.allResourcesService = allResourcesService;
    this.lowestCommonAncestorService = lowestCommonAncestorService;
    this.pageRankCentralityMetricService = pageRankCentralityMetricService;
    this.mapDB = mapDB;
    this.lcaprValueMap = mapDB.hashMap(UID, Serializer.INT_ARRAY, Serializer.DOUBLE)
        .createOrOpen();
  }

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Optional<Integer> resourceAKeyOpt = allResourcesService.getResourceKey(resourcePair.getFirst());
    if (resourceAKeyOpt.isPresent()) {
      Optional<Integer> resourceBKeyOpt = allResourcesService
          .getResourceKey(resourcePair.getSecond());
      if (resourceBKeyOpt.isPresent()) {
        Double value = lcaprValueMap.get(new int[]{resourceAKeyOpt.get(), resourceBKeyOpt.get()});
        return value != null ? value : 0.0;
      }
    }
    return 0.0;
  }

  @Override
  public void compute() {
    for (Resource resourceA : allResourcesService.getResourceList()) {
      Optional<Integer> resourceAKeyOpt = allResourcesService.getResourceKey(resourceA);
      if (resourceAKeyOpt.isPresent()) {
        int resourceAKey = resourceAKeyOpt.get();
        for (Resource resourceB : allResourcesService.getResourceList()) {
          Optional<Integer> resourceBKeyOpt = allResourcesService.getResourceKey(resourceB);
          if (resourceBKeyOpt.isPresent()) {
            int resourceBKey = resourceBKeyOpt.get();
            Set<Resource> lcaClassResource = lowestCommonAncestorService
                .getLowestCommonAncestor(ResourcePair.of(resourceA, resourceB));
            if (!lcaClassResource.isEmpty()) {
              Double optionalSum = lcaClassResource.stream()
                  .map(pageRankCentralityMetricService::getValueFor)
                  .filter(Objects::nonNull).reduce(0.0, (a, b) -> a + b);
              lcaprValueMap.put(new int[]{resourceAKey, resourceBKey},
                  optionalSum / lcaClassResource.size());
            }
          }
        }
      }
    }
    mapDB.commit();
  }

}
