package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LeastCommonSubSummersService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.time.Instant;
import java.util.Iterator;
import java.util.Set;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Graph.Hidden;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link ResnikSimilarityMetricService} which tries to pre-compute the
 * values for all resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = "esm.service.analysis.sim.resnik")
public class ResnikSimilarityMetricServiceImpl implements ResnikSimilarityMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(ResnikSimilarityMetricServiceImpl.class);

  private static final String RESNIK_EDGE_PROP_NAME = "esm.service.analysis.sim.resnik";

  private GremlinService gremlinService;
  private PGS schema;
  private ClassEntropyService classEntropyService;
  private LeastCommonSubSummersService leastCommonSubSummersService;

  public ResnikSimilarityMetricServiceImpl(
      GremlinService gremlinService,
      ClassEntropyService classEntropyService,
      LeastCommonSubSummersService leastCommonSubSummersService) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.classEntropyService = classEntropyService;
    this.leastCommonSubSummersService = leastCommonSubSummersService;
  }

  @Override
  public Double getValueFor(ResourcePair resourcePair) {
    String resourceIRI_A = BlankOrIRIJsonUtil.stringValue(resourcePair.getFirst().value());
    String resourceIRI_B = BlankOrIRIJsonUtil.stringValue(resourcePair.getSecond().value());
    GraphTraversal<Vertex, Vertex> vertexTraversal = gremlinService.traversal().V()
        .has(schema.iri().identifierAsString(), resourceIRI_A);
    if (!vertexTraversal.hasNext()) {
      return null;
    }
    GraphTraversal<Vertex, Object> icTraversal = gremlinService.traversal().V()
        .has(schema.iri().identifierAsString(), resourceIRI_A)
        .out(Hidden.hide(RESNIK_EDGE_PROP_NAME)).as("e")
        .outV().has(schema.iri().identifierAsString(), resourceIRI_B).select("e");
    if (icTraversal.hasNext()) {
      return (Double) ((Edge) icTraversal.next()).properties("value").next().orElse(null);
    } else {
      return computeIC(resourcePair);
    }
  }

  private Double computeIC(ResourcePair pair) {
    Set<Resource> classes = leastCommonSubSummersService.getLeastCommonSubSummersFor(pair);
    return classes.stream().map(clazz -> classEntropyService.getEntropyForClass(clazz))
        .reduce(0.0, (a, b) -> a + b);
  }

  @Override
  public Void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Started to compute the Resnik similarity metric.");
    gremlinService.lock();
    try {
      Iterator<Vertex> verticesAIterator = gremlinService.traversal().getGraph().vertices();
      while (verticesAIterator.hasNext()) {
        Vertex vertexA = verticesAIterator.next();
        Iterator<Vertex> verticesBIterator = gremlinService.traversal().getGraph().vertices();
        while (verticesBIterator.hasNext()) {
          Vertex vertexB = verticesBIterator.next();
          Double clazzSumIC = computeIC(
              ResourcePair.of(new Resource(schema.iri().<String>apply(vertexA)),
                  new Resource(schema.iri().<String>apply(vertexB))));
          vertexA.addEdge(Graph.Hidden.hide(RESNIK_EDGE_PROP_NAME), vertexB, "value", clazzSumIC);
        }
      }
      gremlinService.commit();
    } catch (Exception e) {
      gremlinService.rollback();
    } finally {
      gremlinService.unlock();
    }
    logger.info("Resnik similarity measurement issued on {} computed on {}.", issueTimestamp,
        Instant.now());
    return null;
  }

  @Override
  public AnalysisEventStatus getStatus() {
    return null;
  }
}
