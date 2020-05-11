package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes;

import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.NormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils.Normalizer;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.GremlinService;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.ClassHierarchyService;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
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
 * This is an implementation get {@link ClassEntropyService} using the {@link GremlinService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = ClassEntropyWithGremlinService.UID,
    requiresGremlin = true, prerequisites = {AllClassesService.class,
    ClassHierarchyService.class})
public class ClassEntropyWithGremlinService implements ClassEntropyService {

  private static final Logger logger = LoggerFactory.getLogger(ClassEntropyService.class);

  public static final String UID = "esm.service.analytics.dataset.classentropy.gremlin";

  private final GremlinService gremlinService;
  private final AllClassesService allClassesService;
  private final ClassHierarchyService classHierarchyService;
  private final PGS schema;
  private final DB mapDB;

  private final HTreeMap<String, DecimalNormalizedAnalysisValue> classEntropyMap;

  @Autowired
  public ClassEntropyWithGremlinService(GremlinService gremlinService,
      AllClassesService allClassesService,
      ClassHierarchyService classHierarchyService, DB mapDB) {
    this.gremlinService = gremlinService;
    this.allClassesService = allClassesService;
    this.classHierarchyService = classHierarchyService;
    this.mapDB = mapDB;
    this.classEntropyMap = mapDB.hashMap(UID, Serializer.STRING, Serializer.JAVA)
        .createOrOpen();
    this.schema = gremlinService.getPropertyGraphSchema();
  }

  @Override
  public DecimalNormalizedAnalysisValue getEntropyForClass(Resource resource) {
    return classEntropyMap.get(resource.getId());
  }

  @Override
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to computes information content metric for classes.");
    gremlinService.lock();
    try {
      Set<Resource> allClasses = allClassesService.getAllClasses();
      if (!allClasses.isEmpty()) {
        Normalizer<String> normalizer = new Normalizer<>();
        Optional<Long> totalOpt = gremlinService.traversal().V().dedup().count().tryNext();
        if (totalOpt.isPresent()) {
          long total = totalOpt.get();
          for (Resource classResource : allClassesService.getAllClasses()) {
            Set<Resource> classList = classHierarchyService.getSubClasses(classResource);
            classList.add(classResource);
            GraphTraversal<Vertex, Long> g = gremlinService.traversal().V()
                .has(schema.iri().identifierAsString(),
                    P.within(classList.stream().map(Resource::getId).toArray(String[]::new)))
                .in("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").dedup().count();
            if (g.hasNext()) {
              Long classInstancesNumber = g.next();
              if (classInstancesNumber != null && classInstancesNumber > 0) {
                normalizer.register(classResource.getId(),
                    -Math.log(((double) classInstancesNumber / total)));
              } else {
                normalizer.register(classResource.getId(), -Math.log(0.1 / total));
              }
            } else {
              normalizer.register(classResource.getId(), -Math.log(0.1 / total));
            }
          }

          classEntropyMap.putAll(normalizer.normalize());
          mapDB.commit();
        }
      }
      gremlinService.commit();
    } catch (Exception e) {
      gremlinService.rollback();
      throw e;
    } finally {
      gremlinService.unlock();
    }
    logger.info("Information Content for classes issued on {} computed on {}.", issueTimestamp,
        Instant.now());
  }

}
