package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

  private static final int LOAD_SIZE = 100000;

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
    gremlinService.lock();
    try {
      int n = 0, total = 0;
      Map<Integer, Long> degreeIntermediateMap = new HashMap<>();
      for (Resource resource : allResourcesService.getResourceList()) {
        Optional<Integer> resourceKeyOptional = allResourcesService.getResourceKey(resource);
        if (resourceKeyOptional.isPresent()) {
          Optional<Long> degreeCountOptional = gremlinService.traversal().V()
              .has(schema.iri().identifierAsString(), resource.getId())
              .inE().count().tryNext();
          if (degreeCountOptional.isPresent()) {
            degreeIntermediateMap.put(resourceKeyOptional.get(), degreeCountOptional.get());
            n++;
            total++;
          }
        }
        if (n == LOAD_SIZE) {
          degreeMap.putAll(degreeIntermediateMap);
          logger.debug("Computed degree for {} resources. Degree computed for {} resources in total.",
              LOAD_SIZE, total);
          n = 0;
          degreeIntermediateMap = new HashMap<>();
        }
      }
      if (!degreeIntermediateMap.isEmpty()) {
        degreeMap.putAll(degreeIntermediateMap);
        logger.debug("Computed degree for {} resources. Degree computed for {} resources in total.",
            degreeIntermediateMap.size(), total);
      }
      mapDB.commit();
      gremlinService.commit();
    } catch (Exception e) {
      gremlinService.rollback();
      throw e;
    } finally {
      gremlinService.unlock();
    }
  }

}
