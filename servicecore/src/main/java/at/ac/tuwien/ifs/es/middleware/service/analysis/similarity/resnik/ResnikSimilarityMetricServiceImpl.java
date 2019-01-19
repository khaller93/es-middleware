package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LeastCommonSubSumersService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
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
@AnalyticalProcessing(name = ResnikSimilarityMetricServiceImpl.RESNIK_SIMILARITY_UID)
public class ResnikSimilarityMetricServiceImpl implements ResnikSimilarityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(ResnikSimilarityMetricServiceImpl.class);

  public static final String RESNIK_SIMILARITY_UID = "esm.service.analysis.sim.resnik";

  private static final int LOAD_LIMIT = 25000;

  private static final String ALL_RESOURCE_IRIS_QUERY = "SELECT DISTINCT ?resource WHERE {\n"
      + "    {?resource ?p1 _:o1}\n"
      + "     UNION\n"
      + "    {\n"
      + "        _:o2 ?p2 ?resource .\n"
      + "        FILTER (isIRI(?resource)) .\n"
      + "    } \n"
      + "}\n"
      + "OFFSET ${offset}\n"
      + "LIMIT ${limit}";


  private final SPARQLService sparqlService;
  private final ClassEntropyService classEntropyService;
  private final LeastCommonSubSumersService leastCommonSubSumersService;
  private final DB mapDB;
  private final AnalysisPipelineProcessor processor;

  private final BTreeMap<Object[], Double> resnikValueMap;

  @Autowired
  public ResnikSimilarityMetricServiceImpl(
      SPARQLService sparqlService,
      ClassEntropyService classEntropyService,
      LeastCommonSubSumersService leastCommonSubSumersService,
      DB mapDB, AnalysisPipelineProcessor processor) {
    this.sparqlService = sparqlService;
    this.classEntropyService = classEntropyService;
    this.leastCommonSubSumersService = leastCommonSubSumersService;
    this.mapDB = mapDB;
    this.resnikValueMap = mapDB.treeMap(RESNIK_SIMILARITY_UID)
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.DOUBLE).createOrOpen();
    this.processor = processor;
  }

  @PostConstruct
  private void setUp() {
    processor.registerAnalysisService(this, true, false, false,
        Sets.newHashSet(ClassEntropyService.class, LeastCommonSubSumersService.class));
  }

  private static Object[] simKey(ResourcePair resourcePair) {
    return new Object[]{
        resourcePair.getFirst(),
        resourcePair.getSecond()
    };
  }

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    checkArgument(resourcePair != null, "The given resource pair must not be null.");
    Double value = resnikValueMap.get(simKey(resourcePair));
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
    Set<Resource> resourceSet = new HashSet<>();
    int offset = 0;
    List<Map<String, RDFTerm>> results;
    String resourceQuery = new StrSubstitutor(Collections.singletonMap("limit", LOAD_LIMIT))
        .replace(ALL_RESOURCE_IRIS_QUERY);
    do {
      results = sparqlService.<SelectQueryResult>query(
          new StrSubstitutor(Collections.singletonMap("offset", offset)).replace(resourceQuery),
          true).value();
      if (results != null) {
        results.stream().map(row -> new Resource((BlankNodeOrIRI) row.get("resource")))
            .forEach(resourceSet::add);
        offset += results.size();
      } else {
        break;
      }
    } while (results.size() == LOAD_LIMIT);
    /* compute Resnik metric for resource pairs */
    logger.debug("Size: {}", resourceSet.size());
    final long bulkSize = 100000;
    Map<Object[], Double> metricResultsBulk = new HashMap<>();
    for (Resource resourceA : resourceSet) {
      for (Resource resourceB : resourceSet) {
        ResourcePair pair = ResourcePair.of(resourceA, resourceB);
        metricResultsBulk.put(simKey(pair), computeIC(pair));
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
