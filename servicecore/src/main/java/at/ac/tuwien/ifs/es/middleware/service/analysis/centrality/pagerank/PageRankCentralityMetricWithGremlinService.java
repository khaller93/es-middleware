package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.util.Optional;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
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
@RegisterForAnalyticalProcessing(name = "esm.service.analytics.centrality.pagerank",
    requiresGremlin = true, prerequisites = {AllResourcesService.class})
public class PageRankCentralityMetricWithGremlinService implements PageRankCentralityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(PageRankCentralityMetricWithGremlinService.class);

  private final String PAGE_RANK_PROP_NAME = "esm.service.analytics.centrality.pagerank";

  private final GremlinService gremlinService;
  private final AllResourcesService allResourcesService;
  private final DB mapDB;
  private final PGS schema;

  private HTreeMap<Integer, Double> pageRankMap;

  @Autowired
  public PageRankCentralityMetricWithGremlinService(GremlinService gremlinService,
      AllResourcesService allResourcesService,
      DB mapDB) {
    this.gremlinService = gremlinService;
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.pageRankMap = mapDB.hashMap(PAGE_RANK_PROP_NAME, Serializer.INTEGER, Serializer.DOUBLE)
        .createOrOpen();

  }

  @Override
  public Double getValueFor(Resource resource) {
    Optional<Integer> optionalResourceKey = allResourcesService.getResourceKey(resource);
    if (optionalResourceKey.isPresent()) {
      return pageRankMap.get(optionalResourceKey.get());
    } else {
      return null;
    }
  }

  @Override
  public void compute() {
    logger.info("Starting to compute page rank metric.");
    gremlinService.lock();
    try {
      gremlinService.traversal().withComputer().V().pageRank()
          .group()
          .by(__.map(traverser -> schema.iri().<String>apply((Element) traverser.get())))
          .by(__.values(PageRankVertexProgram.PAGE_RANK)).next().forEach((iri, value) -> {
        Optional<Integer> optionalResourceKey = allResourcesService
            .getResourceKey(new Resource((String) iri));
        if (optionalResourceKey.isPresent()) {
          pageRankMap.put(optionalResourceKey.get(), (Double) value);
        } else {
          logger.warn("No mapped key can be found for {}.", iri);
        }
      });
      mapDB.commit();
      gremlinService.commit();
    } catch (Exception e) {
      gremlinService.rollback();
      throw e;
    } finally {
      gremlinService.unlock();
    }
    logger.info("Page rank has successfully been computed.");
  }

}
