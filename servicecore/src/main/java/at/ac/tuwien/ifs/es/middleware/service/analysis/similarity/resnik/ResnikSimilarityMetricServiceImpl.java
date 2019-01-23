package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LeastCommonSubSumersService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.RP;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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
@RegisterForAnalyticalProcessing(name = ResnikSimilarityMetricServiceImpl.RESNIK_SIMILARITY_UID,
    requiredAnalysisServices = {ClassEntropyService.class, LeastCommonSubSumersService.class,
        AllResourcesService.class})
public class ResnikSimilarityMetricServiceImpl implements ResnikSimilarityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(ResnikSimilarityMetricServiceImpl.class);

  public static final String RESNIK_SIMILARITY_UID = "esm.service.analysis.sim.resnik";

  private final ClassEntropyService classEntropyService;
  private final LeastCommonSubSumersService leastCommonSubSumersService;
  private final AllResourcesService allResourcesService;
  private final DB mapDB;

  private final HTreeMap<RP, Double> resnikValueMap;

  @Autowired
  public ResnikSimilarityMetricServiceImpl(
      SPARQLService sparqlService,
      ClassEntropyService classEntropyService,
      LeastCommonSubSumersService leastCommonSubSumersService,
      AllResourcesService allResourcesService,
      DB mapDB) {
    this.classEntropyService = classEntropyService;
    this.leastCommonSubSumersService = leastCommonSubSumersService;
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.resnikValueMap = mapDB.hashMap(RESNIK_SIMILARITY_UID)
        .keySerializer(Serializer.JAVA).valueSerializer(Serializer.DOUBLE).createOrOpen();
  }

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Double value = resnikValueMap.get(RP.of(resourcePair));
    if (value != null) {
      return value;
    } else {
      return computeIC(resourcePair);
    }
  }

  private Double computeIC(ResourcePair pair) {
    Set<Resource> classes = leastCommonSubSumersService.getLeastCommonSubSumersFor(pair);
    return classes.stream().map(classEntropyService::getEntropyForClass)
        .reduce(0.0, BinaryOperator.maxBy(Double::compareTo));
  }

  @Override
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Started to compute the Resnik similarity metric.");
    /* compute Resnik metric for resource pairs */
    final long bulkSize = 100000;
    Map<RP, Double> metricResultsBulk = new HashMap<>();
    for (Resource resourceA : allResourcesService.getResourceList()) {
      for (Resource resourceB : allResourcesService.getResourceList()) {
        ResourcePair pair = ResourcePair.of(resourceA, resourceB);
        metricResultsBulk.put(RP.of(pair), computeIC(pair));
        if (metricResultsBulk.size() == bulkSize) {
          logger.debug("Bulk loaded {} Resnik metric results.", bulkSize);
          resnikValueMap.putAll(metricResultsBulk);
          metricResultsBulk = new HashMap<>();
        }
      }
    }
    if (!metricResultsBulk.isEmpty()) {
      logger.debug("Bulk loaded {} Resnik results.", metricResultsBulk.size());
      resnikValueMap.putAll(metricResultsBulk);
    }
    mapDB.commit();
    logger.info("Resnik similarity measurement issued on {} computed on {}.", issueTimestamp,
        Instant.now());
  }

}
