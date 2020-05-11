package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils.Normalizer;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.GremlinService;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import java.util.Optional;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import org.apache.tinkerpop.gremlin.structure.Vertex;
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
@RegisterForAnalyticalProcessing(name = PageRankCentralityMetricWithGremlinService.PAGE_RANK_PROP_NAME,
    requiresGremlin = true, prerequisites = {AllResourcesService.class})
public class PageRankCentralityMetricWithGremlinService implements PageRankCentralityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(PageRankCentralityMetricWithGremlinService.class);

  final static String PAGE_RANK_PROP_NAME = "esm.service.analytics.centrality.pagerank";

  private final GremlinService gremlinService;
  private final AllResourcesService allResourcesService;
  private final DB mapDB;
  private final PGS schema;

  private HTreeMap<Integer, DecimalNormalizedAnalysisValue> pageRankMap;

  @Autowired
  public PageRankCentralityMetricWithGremlinService(GremlinService gremlinService,
      AllResourcesService allResourcesService, DB mapDB) {
    this.gremlinService = gremlinService;
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.pageRankMap = mapDB.hashMap(PAGE_RANK_PROP_NAME, Serializer.INTEGER, Serializer.JAVA)
        .createOrOpen();
  }

  @Override
  public DecimalNormalizedAnalysisValue getValueFor(Resource resource) {
    checkArgument(resource != null, "The given resource must not for null.");
    Optional<Integer> optionalResourceKey = allResourcesService.getResourceKey(resource);
    return optionalResourceKey.map(integer -> pageRankMap.get(integer)).orElse(null);
  }

  @Override
  public void compute() {
    logger.info("Starting to compute page rank metric.");
    gremlinService.lock();
    try {
      Normalizer<Integer> normalizer = new Normalizer<>();
      gremlinService.traversal().withComputer().V().pageRank().sideEffect(vertexTraverser -> {
        Vertex vertex = vertexTraverser.get();
        allResourcesService.getResourceKey(new Resource(schema.iri().<String>apply(vertex)))
            .ifPresent(integer -> normalizer.register(integer,
                (Double) vertex.values(PageRankVertexProgram.PAGE_RANK).next()));
      }).iterate();
      pageRankMap.putAll(normalizer.normalize());
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
