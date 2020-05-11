package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.lca.LowestCommonAncestorService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils.Normalizer;
import java.math.BigDecimal;
import java.math.MathContext;
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
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This class is an implementation of {@link LCAPRMetricService} that pre-computes this metric using
 * the {@link LowestCommonAncestorService} and {@link PageRankCentralityMetricService}. The
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = LCAPRMetricImpl.UID, prerequisites = {
    LowestCommonAncestorService.class, PageRankCentralityMetricService.class}, disabled=true)
public class LCAPRMetricImpl implements LCAPRMetricService {

  private static final Logger logger = LoggerFactory.getLogger(LCAPRMetricImpl.class);

  static final String UID = "esm.service.analysis.sim.lcapr";

  private final AllResourcesService allResourcesService;
  private final LowestCommonAncestorService lowestCommonAncestorService;
  private final PageRankCentralityMetricService pageRankCentralityMetricService;

  private final DB mapDB;
  private final HTreeMap<int[], DecimalNormalizedAnalysisValue> lcaprValueMap;

  @Autowired
  public LCAPRMetricImpl(
      AllResourcesService allResourcesService,
      LowestCommonAncestorService lowestCommonAncestorService,
      PageRankCentralityMetricService pageRankCentralityMetricService, DB mapDB) {
    this.allResourcesService = allResourcesService;
    this.lowestCommonAncestorService = lowestCommonAncestorService;
    this.pageRankCentralityMetricService = pageRankCentralityMetricService;
    this.mapDB = mapDB;
    this.lcaprValueMap = mapDB.hashMap(UID, Serializer.INT_ARRAY, Serializer.JAVA)
        .createOrOpen();
  }

  @Override
  public DecimalNormalizedAnalysisValue getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Optional<Integer> resourceAKeyOpt = allResourcesService.getResourceKey(resourcePair.getFirst());
    if (resourceAKeyOpt.isPresent()) {
      Optional<Integer> resourceBKeyOpt = allResourcesService
          .getResourceKey(resourcePair.getSecond());
      if (resourceBKeyOpt.isPresent()) {
        return lcaprValueMap.get(new int[]{resourceAKeyOpt.get(), resourceBKeyOpt.get()});
      }
    }
    return new DecimalNormalizedAnalysisValue(BigDecimal.ZERO);
  }

  @Override
  public void compute() {
    Normalizer<int[]> normalizer = new Normalizer<>();
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
              BigDecimal totalSum = BigDecimal.valueOf(0);
              for (Resource r : lcaClassResource) {
                DecimalNormalizedAnalysisValue value = pageRankCentralityMetricService
                    .getValueFor(r);
                if (value != null) {
                  BigDecimal decimalValue = value.getValue();
                  if (decimalValue != null) {
                    totalSum = totalSum.add(decimalValue);
                  }
                }
              }
              normalizer.register(new int[]{resourceAKey, resourceBKey},
                  totalSum
                      .divide(BigDecimal.valueOf(lcaClassResource.size()), MathContext.DECIMAL64));
            }
          }
        }
      }
    }
    lcaprValueMap.putAll(normalizer.normalize());
    mapDB.commit();
  }

}
