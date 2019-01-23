package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

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
@RegisterForAnalyticalProcessing(name = "esm.service.analytics.centrality.pagerank", requiresGremlin = true)
public class PageRankCentralityMetricWithGremlinService implements PageRankCentralityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(PageRankCentralityMetricWithGremlinService.class);

  private final String PAGE_RANK_PROP_NAME = "esm.service.analytics.centrality.pagerank";

  private GremlinService gremlinService;
  private DB mapDB;
  private PGS schema;

  private HTreeMap<String, Double> pageRankMap;

  @Value("${esm.service.analytics.centrality.pagerank:#{false}}")
  private boolean disabled;

  @Autowired
  public PageRankCentralityMetricWithGremlinService(GremlinService gremlinService,
      DB mapDB) {
    this.gremlinService = gremlinService;
    this.mapDB = mapDB;
    this.pageRankMap = mapDB.hashMap(PAGE_RANK_PROP_NAME, Serializer.STRING, Serializer.DOUBLE)
        .createOrOpen();
    this.schema = gremlinService.getPropertyGraphSchema();
  }

  @Override
  public Double getValueFor(Resource resource) {
    return pageRankMap.get(resource.getId());
  }

  @Override
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to compute page rank metric.");
    gremlinService.lock();
    try {
      gremlinService.traversal().withComputer().V().pageRank()
          .group()
          .by(__.map(traverser -> schema.iri().<String>apply((Element) traverser.get())))
          .by(__.values(PageRankVertexProgram.PAGE_RANK)).next().forEach((iri, value) -> {
        pageRankMap.put((String) iri, (Double) value);
      });
      mapDB.commit();
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
