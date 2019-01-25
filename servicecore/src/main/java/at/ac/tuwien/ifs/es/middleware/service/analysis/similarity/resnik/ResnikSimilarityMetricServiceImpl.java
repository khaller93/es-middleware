package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.LeastCommonSubsumersService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.RP;
import java.time.Instant;
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
@Primary
@Service
@RegisterForAnalyticalProcessing(name = ResnikSimilarityMetricServiceImpl.UID,
    prerequisites = {ClassEntropyService.class, LeastCommonSubsumersService.class,
        AllResourcesService.class})
public class ResnikSimilarityMetricServiceImpl implements ResnikSimilarityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(ResnikSimilarityMetricServiceImpl.class);

  public static final String UID = "esm.service.analysis.sim.resnik";

  private final ClassEntropyService classEntropyService;
  private final LeastCommonSubsumersService leastCommonSubSumersService;
  private final AllResourcesService allResourcesService;
  private final DB mapDB;

  private final HTreeMap<int[], Double> resnikValueMap;

  @Autowired
  public ResnikSimilarityMetricServiceImpl(
      ClassEntropyService classEntropyService,
      LeastCommonSubsumersService leastCommonSubSumersService,
      AllResourcesService allResourcesService,
      DB mapDB) {
    this.classEntropyService = classEntropyService;
    this.leastCommonSubSumersService = leastCommonSubSumersService;
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.resnikValueMap = mapDB.hashMap(UID, Serializer.INT_ARRAY, Serializer.DOUBLE)
        .createOrOpen();
  }

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Optional<Integer> optionalResourceAKey = allResourcesService
        .getResourceKey(resourcePair.getFirst());
    if (optionalResourceAKey.isPresent()) {
      Optional<Integer> optionalResourceBKey = allResourcesService
          .getResourceKey(resourcePair.getSecond());
      if (optionalResourceBKey.isPresent()) {
        Double value = resnikValueMap
            .get(new int[]{optionalResourceAKey.get(), optionalResourceBKey.get()});
        if (value != null) {
          return value;
        }
      }
    }
    return computeIC(resourcePair);
  }

  private Double computeIC(ResourcePair pair) {
    Set<Resource> classes = leastCommonSubSumersService.getLeastCommonSubsumersFor(pair);
    return classes.stream().map(classEntropyService::getEntropyForClass)
        .reduce(0.0, BinaryOperator.maxBy(Double::compareTo));
  }

  @Override
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Started to compute the Resnik similarity metric.");
    /* compute Resnik metric for resource pairs */
    long bulkSize = 500000L;
    Map<int[], Double> metricResultsBulk = new HashMap<>();
    for (Resource resourceA : allResourcesService.getResourceMap()) {
      Optional<Integer> optionalResourceAKey = allResourcesService.getResourceKey(resourceA);
      if (optionalResourceAKey.isPresent()) {
        int resourceAKey = optionalResourceAKey.get();
        for (Resource resourceB : allResourcesService.getResourceMap()) {
          Optional<Integer> optionalResourceBKey = allResourcesService.getResourceKey(resourceB);
          if (optionalResourceBKey.isPresent()) {
            int resourceBKey = optionalResourceBKey.get();
            ResourcePair pair = ResourcePair.of(resourceA, resourceB);
            metricResultsBulk.put(new int[]{resourceAKey, resourceBKey}, computeIC(pair));
            if (metricResultsBulk.size() == bulkSize) {
              logger.debug("Bulk loaded {} Resnik metric results.", bulkSize);
              resnikValueMap.putAll(metricResultsBulk);
              metricResultsBulk = new HashMap<>();
            }
          } else {
            logger.warn("No mapped key can be found for {}.", resourceA);
          }
        }
      } else {
        logger.warn("No mapped key can be found for {}.", resourceA);
      }
    }
    if (!metricResultsBulk.isEmpty()) {
      logger.debug("Bulk loaded {} Resnik results.", metricResultsBulk.size());
      resnikValueMap.putAll(metricResultsBulk);
    }
    mapDB.commit();
    logger.info("Resnik similarity measurement has successfully been computed.");
  }

}
