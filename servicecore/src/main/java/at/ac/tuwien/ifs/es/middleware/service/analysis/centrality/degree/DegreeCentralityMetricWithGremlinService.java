package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.CentralityMetricStoreService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.entity.CentralityMetricKey;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.entity.CentralityMetricResult;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is an implementation of {@link DegreeCentralityMetricService} that uses the {@link
 * at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService} to compute the
 * degree of resources.
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
  private CentralityMetricStoreService centralityMetricStoreService;
  private PGS schema;
  private AnalysisPipelineProcessor processor;

  @Autowired
  public DegreeCentralityMetricWithGremlinService(GremlinService gremlinService,
      CentralityMetricStoreService centralityMetricStoreService,
      AnalysisPipelineProcessor processor) {
    this.gremlinService = gremlinService;
    this.centralityMetricStoreService = centralityMetricStoreService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.processor = processor;
  }

  @PostConstruct
  private void setUp() {
    processor.registerAnalysisService(this, false, false, true, null);
  }

  @Override
  public Long getValueFor(Resource resource) {
    Optional<CentralityMetricResult> optionalDegreeValue = centralityMetricStoreService
        .findById(CentralityMetricKey.of(DEGREE_PROP_NAME, resource));
    return optionalDegreeValue.map(CentralityMetricResult::<Long>getValue).orElse(null);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to compute degree metric.");
    centralityMetricStoreService.saveAll(gremlinService
        .traversal().V().group()
        .by(__.map(traverser -> schema.iri().<String>apply((Element) traverser.get())))
        .by(__.inE().count()).next().entrySet().stream()
        .map(e -> new CentralityMetricResult(
            CentralityMetricKey.of(DEGREE_PROP_NAME, (String) e.getKey()),
            (Number) e.getValue())).collect(Collectors.toList()));
    logger.info("Degree metric issued on {} computed on {}.", issueTimestamp, Instant.now());
  }

}
