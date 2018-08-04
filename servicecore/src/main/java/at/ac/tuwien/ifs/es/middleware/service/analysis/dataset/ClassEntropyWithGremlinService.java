package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.DatasetInformationService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link ClassEntropyService} using the {@link GremlinService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = "esm.service.analytics.dataset.classentropy")
public class ClassEntropyWithGremlinService implements ClassEntropyService {

  private static final Logger logger = LoggerFactory.getLogger(ClassEntropyService.class);

  public static final String ENTROPY_PROP_NAME = "esm.class.ic";

  private GremlinService gremlinService;
  private DatasetInformationService datasetInformationService;
  private PGS schema;
  private ApplicationEventPublisher eventPublisher;
  private TaskExecutor taskExecutor;

  private long lastUpdateTimestamp = 0L;
  private Lock computationLock = new ReentrantLock();

  public ClassEntropyWithGremlinService(GremlinService gremlinService,
      DatasetInformationService datasetInformationService,
      ApplicationEventPublisher eventPublisher, TaskExecutor taskExecutor) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.datasetInformationService = datasetInformationService;
    this.eventPublisher = eventPublisher;
    this.taskExecutor = taskExecutor;
  }

  @EventListener
  public void onApplicationEvent(GremlinDAOReadyEvent event) {
    logger.debug("Recognized an Gremlin ready event {}.", event);
    startComputation(event.getTimestamp());
  }

  @EventListener
  public void onApplicationEvent(GremlinDAOUpdatedEvent event) {
    logger.debug("Recognized an Gremlin update event {}.", event);
    startComputation(event.getTimestamp());
  }

  private void startComputation(long eventTimestamp) {
    computationLock.lock();
    try {
      if (lastUpdateTimestamp < eventTimestamp) {
        taskExecutor.execute(this::compute);
        lastUpdateTimestamp = eventTimestamp;
      }
    } finally {
      computationLock.unlock();
    }
  }

  @Override
  public Double getEntropyForClass(Resource resource) {
    String resourceIRI = BlankOrIRIJsonUtil.stringValue(resource.value());
    GraphTraversal<Vertex, Vertex> traversal = gremlinService.traversal().V()
        .has(schema.iri().identifierAsString(), resourceIRI);
    if (!traversal.hasNext()) {
      return null;
    }
    return (Double) traversal.next().property(ENTROPY_PROP_NAME).orElse(null);
  }

  @Override
  public Void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to computes information content metric for classes.");
    gremlinService.lock();
    try {
      List<Resource> allClasses = datasetInformationService.getAllClasses();
      if (!allClasses.isEmpty()) {
        GraphTraversalSource g = gremlinService.traversal();
        Long total = g.V().dedup().count().next();
        String[] addClassLabels = allClasses.stream().skip(1).map(Resource::getId)
            .toArray(String[]::new);
        Map<Object, Object> classInstancesMap = g
            .V().has(schema.iri().identifierAsString(), allClasses.get(0).getId(), addClassLabels)
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
              .property(Cardinality.single, ENTROPY_PROP_NAME,
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
    return null;
  }

  @Override
  public AnalysisEventStatus getStatus() {
    return null;
  }

}
