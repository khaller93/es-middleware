package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
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
@AnalyticalProcessing(name = ClassEntropyWithGremlinService.CLASS_ENTROPY_UID)
public class ClassEntropyWithGremlinService implements ClassEntropyService {

  private static final Logger logger = LoggerFactory.getLogger(ClassEntropyService.class);

  public static final String CLASS_ENTROPY_UID = "esm.service.analytics.dataset.classentropy";

  private GremlinService gremlinService;
  private ClassInformationService classInformationService;
  private PGS schema;
  private AnalysisPipelineProcessor processor;

  @Autowired
  public ClassEntropyWithGremlinService(GremlinService gremlinService,
      ClassInformationService classInformationService,
      AnalysisPipelineProcessor processor) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.classInformationService = classInformationService;
    this.processor = processor;
  }

  @PostConstruct
  private void setUp() {
    processor.registerAnalysisService(this, false, false, true,
        Sets.newHashSet(ClassInformationService.class));
  }

  @Override
  public Double getEntropyForClass(Resource resource) {
    String resourceIRI = BlankOrIRIJsonUtil.stringValue(resource.value());
    GraphTraversal<Vertex, Vertex> traversal = gremlinService.traversal().V()
        .has(schema.iri().identifierAsString(), resourceIRI);
    if (!traversal.hasNext()) {
      return null;
    }
    return (Double) traversal.next().property(CLASS_ENTROPY_UID).orElse(null);
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
          gremlinService.traversal().V().has(schema.iri().identifierAsString(),
              BlankOrIRIJsonUtil.stringValue(clazz.value()))
              .property(Cardinality.single, CLASS_ENTROPY_UID,
                  icClassMap.getOrDefault(clazz, -Math.log(1.0 / total))).iterate();
        }
        gremlinService.commit();
      }
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
