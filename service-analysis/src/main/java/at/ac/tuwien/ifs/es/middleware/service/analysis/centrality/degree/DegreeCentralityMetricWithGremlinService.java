package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils.Normalizer;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.GremlinService;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import java.util.Optional;
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
 * GremlinService} to compute the degree get resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = DegreeCentralityMetricWithGremlinService.DEGREE_PROP_NAME,
    requiresGremlin = true, prerequisites = {AllResourcesService.class})
public class DegreeCentralityMetricWithGremlinService implements DegreeCentralityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(DegreeCentralityMetricWithGremlinService.class);

  static final String DEGREE_PROP_NAME = "esm.service.analytics.centrality.degree";

  private final GremlinService gremlinService;
  private final AllResourcesService allResourcesService;
  private DB mapDB;
  private PGS schema;

  private HTreeMap<Integer, DecimalNormalizedAnalysisValue> degreeMap;

  @Autowired
  public DegreeCentralityMetricWithGremlinService(GremlinService gremlinService,
      AllResourcesService allResourcesService, DB mapDB) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.degreeMap = mapDB.hashMap(DEGREE_PROP_NAME, Serializer.INTEGER, Serializer.JAVA)
        .createOrOpen();
  }

  @Override
  public DecimalNormalizedAnalysisValue getValueFor(Resource resource) {
    checkArgument(resource != null,
        "The passed resource (for which the degree shall be returned) must not be null.");
    return allResourcesService.getResourceKey(resource).map(integer -> degreeMap.get(integer))
        .orElse(null);
  }

  @Override
  public void compute() {
    gremlinService.lock();
    try {
      Normalizer<Integer> normalizer = new Normalizer<>();
      for (Resource resource : allResourcesService.getResourceList()) {
        Optional<Integer> resourceKeyOptional = allResourcesService.getResourceKey(resource);
        if (resourceKeyOptional.isPresent()) {
          Optional<Long> degreeCountOptional = gremlinService.traversal().V()
              .has(schema.iri().identifierAsString(), resource.getId())
              .inE().count().tryNext();
          degreeCountOptional
              .ifPresent(aLong -> normalizer.register(resourceKeyOptional.get(), aLong));
        }
      }
      degreeMap.putAll(normalizer.normalize());
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
