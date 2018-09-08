package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.event.PageRankUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PostConstruct;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * This class is an implementation of {@link PageRankCentralityMetricService} using Tinkerprop
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
  private PGS schema;
  private AnalysisPipelineProcessor processor;

  @Autowired
  public PageRankCentralityMetricWithGremlinService(GremlinService gremlinService,
      AnalysisPipelineProcessor processor) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.processor = processor;
  }

  @PostConstruct
  private void setUp() {
    processor.registerAnalysisService(this, false, false, true, null);
  }

  @Override
  public Double getValueFor(Resource resource) {
    String resourceIRI = BlankOrIRIJsonUtil.stringValue(resource.value());
    GraphTraversal<Vertex, Vertex> traversal = gremlinService.traversal().V()
        .has(schema.iri().identifierAsString(), resourceIRI);
    if (!traversal.hasNext()) {
      return null;
    }
    return (Double) traversal.next().property(PAGE_RANK_PROP_NAME).orElse(null);
  }

  @Override
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to computes page rank metric.");
    gremlinService.lock();
    try {
      Map<Object, Object> pageRankMap = gremlinService.traversal().withComputer().V().pageRank()
          .group()
          .by(__.id())
          .by(__.values(PageRankVertexProgram.PAGE_RANK)).next();
      for (Map.Entry<Object, Object> entry : pageRankMap.entrySet()) {
        gremlinService.traversal().V(entry.getKey())
            .property(Cardinality.single, PAGE_RANK_PROP_NAME, entry.getValue()).iterate();
      }
      gremlinService.commit();
    } catch (Exception e) {
      gremlinService.rollback();
      throw e;
    } finally {
      gremlinService.unlock();
    }
    logger.info("Page rank issued on {} computed on {}.", issueTimestamp, Instant.now());
  }

}
