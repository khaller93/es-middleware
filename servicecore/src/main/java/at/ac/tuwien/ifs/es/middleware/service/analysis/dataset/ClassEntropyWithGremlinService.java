package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
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
 * This is an implementation get {@link ClassEntropyService} using the {@link GremlinService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = ClassEntropyWithGremlinService.UID,
    requiresGremlin = true, prerequisites = {ClassInformationService.class})
public class ClassEntropyWithGremlinService implements ClassEntropyService {

  private static final Logger logger = LoggerFactory.getLogger(ClassEntropyService.class);

  public static final String UID = "esm.service.analytics.dataset.classentropy";

  private final GremlinService gremlinService;
  private final ClassInformationService classInformationService;
  private final PGS schema;
  private final DB mapDB;

  private final HTreeMap<String, Double> classEntropyMap;

  @Autowired
  public ClassEntropyWithGremlinService(GremlinService gremlinService,
      ClassInformationService classInformationService,
      DB mapDB) {
    this.gremlinService = gremlinService;
    this.classInformationService = classInformationService;
    this.mapDB = mapDB;
    this.classEntropyMap = mapDB.hashMap(UID, Serializer.STRING, Serializer.DOUBLE)
        .createOrOpen();
    this.schema = gremlinService.getPropertyGraphSchema();
  }

  @Override
  public Double getEntropyForClass(Resource resource) {
    return classEntropyMap.get(resource.getId());
  }

  @Override
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to computes information content metric for classes.");
    gremlinService.lock();
    try {
      Set<Resource> allClasses = classInformationService.getAllClasses();
      if (!allClasses.isEmpty()) {
        GraphTraversalSource g = gremlinService.traversal();
        Long total = g.V().dedup().count().next();
        String[] addClassLabels = allClasses.stream().skip(1).map(Resource::getId)
            .toArray(String[]::new);
        Map<Object, Object> classInstancesMap = g
            .V().has(schema.iri().identifierAsString(), allClasses.iterator().next().getId(),
                addClassLabels)
            .until(__.or(__.not(__.in("http://www.w3.org/2000/01/rdf-schema#subClassOf")),
                __.cyclicPath()))
            .repeat(__.in("http://www.w3.org/2000/01/rdf-schema#subClassOf")).group()
            .by(__.map(traverser -> schema.iri().<String>apply((Element) traverser.get())))
            .by(__.in("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").dedup().count()).next();
        Map<Resource, Double> icClassMap = classInstancesMap.entrySet().stream().collect(
            Collectors.toMap(e -> new Resource((String) e.getKey()),
                e -> {
                  Double p = (((((Long) e.getValue()).doubleValue()) + 1.0) / total);
                  return -Math.log(p);
                }));
        for (Resource clazz : allClasses) {
          classEntropyMap
              .put(clazz.getId(), icClassMap.getOrDefault(clazz, -Math.log(1.0 / total)));
        }
        mapDB.commit();
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
