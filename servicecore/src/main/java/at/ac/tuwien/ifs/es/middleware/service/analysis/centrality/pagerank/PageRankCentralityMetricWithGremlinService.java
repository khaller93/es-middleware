package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import org.apache.tinkerpop.gremlin.structure.Vertex;
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

  private static final int LOAD_LIMIT = 1000;

  private final GremlinService gremlinService;
  private final AllResourcesService allResourcesService;
  private final DB mapDB;
  private final PGS schema;

  private HTreeMap<Integer, Double> pageRankMap;

  @Autowired
  public PageRankCentralityMetricWithGremlinService(GremlinService gremlinService,
      AllResourcesService allResourcesService,
      @Qualifier("persistent-mapdb") DB mapDB) {
    this.gremlinService = gremlinService;
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.pageRankMap = mapDB.hashMap(PAGE_RANK_PROP_NAME, Serializer.INTEGER, Serializer.DOUBLE)
        .createOrOpen();

  }

  @Override
  public Double getValueFor(Resource resource) {
    checkArgument(resource != null, "The given resource must not for null.");
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
      final AtomicInteger n = new AtomicInteger();
      final AtomicInteger total = new AtomicInteger();
      final Map<Integer, Double> pageRankIntermediateMap = new HashMap<>();
      gremlinService.traversal().withComputer().V().pageRank().sideEffect(vertexTraverser -> {
        Vertex vertex = vertexTraverser.get();
        Optional<Integer> resourceKeyOptional = allResourcesService
            .getResourceKey(new Resource(schema.iri().<String>apply(vertex)));
        if (resourceKeyOptional.isPresent()) {
          pageRankIntermediateMap.put(resourceKeyOptional.get(),
              (Double) vertex.values(PageRankVertexProgram.PAGE_RANK).next());
          n.addAndGet(1);
          total.addAndGet(1);
        }
        boolean load = n.compareAndSet(LOAD_LIMIT, 0);
        if (load) {
          pageRankMap.putAll(pageRankIntermediateMap);
          pageRankIntermediateMap.clear();
          logger
              .debug(
                  "Computed page rank for {} resources. Page Rank computed for {} resources in total.",
                  LOAD_LIMIT, total.get());
        }
      }).iterate();
      if (!pageRankIntermediateMap.isEmpty()) {
        pageRankMap.putAll(pageRankIntermediateMap);
        logger
            .debug(
                "Computed page rank for {} resources. Page Rank computed for {} resources in total.",
                pageRankIntermediateMap.size(), total.get());
      }
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
