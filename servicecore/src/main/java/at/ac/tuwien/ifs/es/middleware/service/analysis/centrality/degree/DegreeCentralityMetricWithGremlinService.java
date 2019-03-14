package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Optional;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
@RegisterForAnalyticalProcessing(name = "esm.service.analytics.centrality.degree",
    requiresGremlin = true, prerequisites = {AllResourcesService.class})
public class DegreeCentralityMetricWithGremlinService implements DegreeCentralityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(DegreeCentralityMetricWithGremlinService.class);

  public static final String DEGREE_PROP_NAME = "esm.service.analytics.centrality.degree";

  private final GremlinService gremlinService;
  private final AllResourcesService allResourcesService;
  private DB mapDB;
  private PGS schema;

  private HTreeMap<Integer, Long> degreeMap;

  @Autowired
  public DegreeCentralityMetricWithGremlinService(GremlinService gremlinService,
      AllResourcesService allResourcesService,
      @Qualifier("persistent-mapdb") DB mapDB) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.degreeMap = mapDB.hashMap(DEGREE_PROP_NAME, Serializer.INTEGER, Serializer.LONG)
        .createOrOpen();
  }

  @Override
  public Long getValueFor(Resource resource) {
    checkArgument(resource != null,
        "The passed resource (for which the degree shall be returned) must not be null.");
    return allResourcesService.getResourceKey(resource).map(integer -> degreeMap.get(integer))
        .orElse(null);
  }

  @Override
  public void compute() {
    logger.info("Starting to compute degree metric.");
    gremlinService.lock();
    try {
      gremlinService
          .traversal().V().group()
          .by(__.map(traverser -> schema.iri().<String>apply((Element) traverser.get())))
          .by(__.inE().count()).next().forEach((iri, value) -> {
        Optional<Integer> resourceKeyOptional = allResourcesService
            .getResourceKey(new Resource((String) iri));
        if (resourceKeyOptional.isPresent()) {
          degreeMap.put(resourceKeyOptional.get(), (Long) value);
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
    logger.info("Degree metric has been successfully computed.");
  }

}
