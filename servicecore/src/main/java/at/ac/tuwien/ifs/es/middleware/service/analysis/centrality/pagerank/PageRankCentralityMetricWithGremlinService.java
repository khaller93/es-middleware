package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality.CentralityMetricStoreRepository;
import at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality.CentralityMetricStoreService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality.entity.CentralityMetricKey;
import at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality.entity.CentralityMetricResult;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
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
 * This class is an implementation get {@link PageRankCentralityMetricService} using Tinkerprop
 * Gremlin {@link GremlinService}. A vertex program will be executed in order to compute the value
 * for each vertex in the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = "esm.service.analytics.centrality.pagerank")
public class PageRankCentralityMetricWithGremlinService implements PageRankCentralityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(PageRankCentralityMetricWithGremlinService.class);

  private final String PAGE_RANK_PROP_NAME = "esm.service.analytics.centrality.pagerank";

  private GremlinService gremlinService;
  private CentralityMetricStoreService centralityMetricStoreService;
  private CentralityMetricStoreRepository centralityMetricStoreRepository;
  private PGS schema;
  private AnalysisPipelineProcessor processor;

  @Autowired
  public PageRankCentralityMetricWithGremlinService(GremlinService gremlinService,
      CentralityMetricStoreService centralityMetricStoreService,
      CentralityMetricStoreRepository centralityMetricStoreRepository,
      AnalysisPipelineProcessor processor) {
    this.gremlinService = gremlinService;
    this.centralityMetricStoreService = centralityMetricStoreService;
    this.centralityMetricStoreRepository = centralityMetricStoreRepository;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.processor = processor;
  }

  @PostConstruct
  private void setUp() {
    //processor.registerAnalysisService(this, false, false, true, null);
  }

  @Override
  public Double getValueFor(Resource resource) {
    Optional<CentralityMetricResult> optionalDegreeValue = centralityMetricStoreService
        .findById(PAGE_RANK_PROP_NAME, resource);
    return optionalDegreeValue.map(CentralityMetricResult::<Double>getValue).orElse(null);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to compute page rank metric.");
    centralityMetricStoreRepository.saveAll(gremlinService.traversal().withComputer().V().pageRank()
        .group()
        .by(__.map(traverser -> schema.iri().<String>apply((Element) traverser.get())))
        .by(__.values(PageRankVertexProgram.PAGE_RANK)).next().entrySet().stream()
        .map(e ->
            centralityMetricStoreService.get(PAGE_RANK_PROP_NAME, (String) e.getKey(),
                (Number) e.getValue())).collect(Collectors.toList()));
    logger.info("Page rank issued on {} computed on {}.", issueTimestamp, Instant.now());
  }

}
