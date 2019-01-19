package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import javax.annotation.PostConstruct;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation get {@link DegreeCentralityMetricService} that uses the {@link
 * at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService} to compute the
 * degree get resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = "esm.service.analytics.centrality.degree")
public class DegreeCentralityMetricWithGremlinService implements DegreeCentralityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(DegreeCentralityMetricWithGremlinService.class);

  public static final String DEGREE_PROP_NAME = "esm.service.analytics.centrality.degree";

  private GremlinService gremlinService;
  private DB mapDB;
  private PGS schema;
  private AnalysisPipelineProcessor processor;

  private HTreeMap<String, Long> degreeMap;

  @Autowired
  public DegreeCentralityMetricWithGremlinService(GremlinService gremlinService,
      DB mapDB, AnalysisPipelineProcessor processor) {
    this.gremlinService = gremlinService;
    this.mapDB = mapDB;
    this.degreeMap = mapDB.hashMap(DEGREE_PROP_NAME, Serializer.STRING, Serializer.LONG)
        .createOrOpen();
    this.schema = gremlinService.getPropertyGraphSchema();
    this.processor = processor;
  }

  @PostConstruct
  private void setUp() {
    processor.registerAnalysisService(this, false, false, true, null);
  }

  @Override
  public Long getValueFor(Resource resource) {
    return degreeMap.get(resource.getId());
  }

  @Override
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to compute degree metric.");
    gremlinService
        .traversal().V().group()
        .by(__.map(traverser -> schema.iri().<String>apply((Element) traverser.get())))
        .by(__.inE().count()).next().forEach((iri, value) -> {
      degreeMap.put((String) iri, (Long) value);
    });
    mapDB.commit();
    logger.info("Degree metric issued on {} computed on {}.", issueTimestamp, Instant.now());
  }

}
