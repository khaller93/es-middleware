package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.lca.LowestCommonAncestorService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils.Normalizer;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
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
 * This is an implementation get {@link ResnikSimilarityMetricService} which tries to pre-compute
 * the values for all resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@RegisterForAnalyticalProcessing(name = ResnikSimilarityMetricServiceImpl.UID,
    prerequisites = {ClassEntropyService.class, LowestCommonAncestorService.class,
        AllResourcesService.class}, disabled=true)
public class ResnikSimilarityMetricServiceImpl implements ResnikSimilarityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(ResnikSimilarityMetricServiceImpl.class);

  static final String UID = "esm.service.analysis.sim.resnik";

  private final ClassEntropyService classEntropyService;
  private final LowestCommonAncestorService leastCommonSubSumersService;
  private final AllResourcesService allResourcesService;
  private final DB mapDB;

  private final HTreeMap<int[], DecimalNormalizedAnalysisValue> resnikValueMap;

  @Autowired
  public ResnikSimilarityMetricServiceImpl(
      ClassEntropyService classEntropyService,
      LowestCommonAncestorService leastCommonSubSumersService,
      AllResourcesService allResourcesService, DB mapDB) {
    this.classEntropyService = classEntropyService;
    this.leastCommonSubSumersService = leastCommonSubSumersService;
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.resnikValueMap = mapDB.hashMap(UID, Serializer.INT_ARRAY, Serializer.JAVA)
        .createOrOpen();
  }

  @Override
  public DecimalNormalizedAnalysisValue getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Optional<Integer> optionalResourceAKey = allResourcesService
        .getResourceKey(resourcePair.getFirst());
    if (optionalResourceAKey.isPresent()) {
      Optional<Integer> optionalResourceBKey = allResourcesService
          .getResourceKey(resourcePair.getSecond());
      if (optionalResourceBKey.isPresent()) {
        DecimalNormalizedAnalysisValue value = resnikValueMap
            .get(new int[]{optionalResourceAKey.get(), optionalResourceBKey.get()});
        if (value != null) {
          return value;
        }
      }
    }
    return new DecimalNormalizedAnalysisValue(BigDecimal.ZERO);
  }

  private Double computeIC(ResourcePair pair) {
    Set<Resource> classes = leastCommonSubSumersService.getLowestCommonAncestor(pair);
    return classes.stream().map(classEntropyService::getEntropyForClass)
        .map(DecimalNormalizedAnalysisValue::getValue)
        .reduce(BigDecimal.ZERO, BinaryOperator.maxBy(BigDecimal::compareTo)).doubleValue();
  }

  @Override
  public void compute() {
    logger.info("Started to compute the Resnik similarity metric.");
    /* compute Resnik metric for resource pairs */
    Normalizer<int[]> normalizer = new Normalizer<>();
    for (Resource resourceA : allResourcesService.getResourceList()) {
      Optional<Integer> optionalResourceAKey = allResourcesService.getResourceKey(resourceA);
      if (optionalResourceAKey.isPresent()) {
        int resourceAKey = optionalResourceAKey.get();
        for (Resource resourceB : allResourcesService.getResourceList()) {
          Optional<Integer> optionalResourceBKey = allResourcesService.getResourceKey(resourceB);
          if (optionalResourceBKey.isPresent()) {
            int resourceBKey = optionalResourceBKey.get();
            ResourcePair pair = ResourcePair.of(resourceA, resourceB);
            normalizer.register(new int[]{resourceAKey, resourceBKey}, computeIC(pair));
          } else {
            logger.warn("No mapped key can be found for {}.", resourceA);
          }
        }
      } else {
        logger.warn("No mapped key can be found for {}.", resourceA);
      }
    }
    resnikValueMap.putAll(normalizer.normalize());
    mapDB.commit();
    logger.info("Resnik similarity measurement has successfully been computed.");
  }

}
