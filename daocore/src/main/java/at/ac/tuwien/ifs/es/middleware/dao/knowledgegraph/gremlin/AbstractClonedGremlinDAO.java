package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;

/**
 * This is a {@link KGGremlinDAO} in which data will be cloned from the {@link KnowledgeGraphDAO}.
 * It implements generic methods and expects simply a new clean {@link Graph} from the implementing
 * class to work.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractClonedGremlinDAO implements KGGremlinDAO,
    ApplicationListener<SPARQLDAOUpdatedEvent> {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryGremlinDAO.class);

  private static final int LOAD_LIMIT = 10000;

  private static final String ALL_STATEMENTS_QUERY = "SELECT DISTINCT ?s ?p ?o WHERE {\n"
      + "  ?s ?p ?o .\n"
      + "  FILTER (!isLiteral(?o)) .\n"
      + "}\n"
      + "OFFSET ${offset}\n"
      + "LIMIT ${limit}";

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  private KGSparqlDAO sparqlDAO;
  private Graph graph;
  private long timestamp;

  private ExecutorService threadPool = Executors.newCachedThreadPool();
  private ReadWriteLock graphLock = new ReentrantReadWriteLock(true);

  private Consumer<GremlinDAOUpdatedEvent> updateTriggerFunction;

  /**
   * Creates a new {@link AbstractClonedGremlinDAO} with the given {@code knowledgeGraphDAO}.
   *
   * @param knowledgeGraphDAO that shall be used.
   */
  public AbstractClonedGremlinDAO(KnowledgeGraphDAO knowledgeGraphDAO) {
    this.sparqlDAO = knowledgeGraphDAO.getSparqlDAO();
  }

  @PostConstruct
  public void setUp() {
    threadPool.submit(new GraphConstruction(Instant.now().getEpochSecond()));
  }

  /**
   * This method should return a new clean instance of a {@link Graph}.
   *
   * @return a new clean instance of a {@link Graph}.
   */
  public abstract Graph newGraphInstance();

  @Override
  public void onApplicationEvent(SPARQLDAOUpdatedEvent event) {
    logger.debug("Recognized an SPARQL update event {}.", event);
    threadPool.submit(new GraphConstruction(event.getTimestamp()));
  }

  @Override
  public GraphTraversalSource traversal() {
    graphLock.readLock().lock();
    try {
      return graph.traversal();
    } finally {
      graphLock.readLock().unlock();
    }
  }

  @Override
  public Features getFeatures() {
    graphLock.readLock().lock();
    try {
      return graph.features();
    } finally {
      graphLock.readLock().unlock();
    }
  }

  @PreDestroy
  public void tearDown() {
    try {
      if (graph != null) {
        graph.close();
      }
      threadPool.shutdown();
    } catch (Exception e) {
      logger.error("Failed to close the InMemoryGremlin graph. {}", e.getMessage());
    }
  }

  /**
   * Constructs the in-memory graph and loads the statements from the {@link KnowledgeGraphDAO}.
   */
  private Graph getGremlinGraphFromKnowledgeGraph() {
    logger.info("Starts to construct an in-memory graph.");
    Graph newGraph = newGraphInstance();
    Map<BlankNodeOrIRI, Vertex> recognizedNodes = new HashMap<>();
    List<Map<String, RDFTerm>> values;
    int offset = 0;
    do {
      Map<String, String> valuesMap = new HashMap<>();
      valuesMap.put("offset", String.valueOf(offset));
      valuesMap.put("limit", String.valueOf(LOAD_LIMIT));
      values = ((SelectQueryResult) sparqlDAO
          .query(new StringSubstitutor(valuesMap).replace(ALL_STATEMENTS_QUERY), true)).value();
      for (Map<String, RDFTerm> row : values) {
        BlankNodeOrIRI sResource = (BlankNodeOrIRI) row.get("s");
        Vertex sVertex = recognizedNodes
            .compute(sResource, (nodeR, vertex) -> vertex != null ? vertex
                : newGraph.addVertex(T.id, BlankOrIRIJsonUtil.stringValue(nodeR)));
        BlankNodeOrIRI oResource = (BlankNodeOrIRI) row.get("o");
        Vertex oVertex = recognizedNodes
            .compute(oResource, (nodeR, vertex) -> vertex != null ? vertex
                : newGraph.addVertex(T.id, BlankOrIRIJsonUtil.stringValue(nodeR)));
        BlankNodeOrIRI property = (BlankNodeOrIRI) row.get("p");
        sVertex.addEdge(BlankOrIRIJsonUtil.stringValue(property), oVertex);
      }
      logger.info("Loaded {} statements from the knowledge graph {}.", offset + values.size(),
          sparqlDAO.getClass().getSimpleName());
      offset += LOAD_LIMIT;
    } while (!values.isEmpty() && values.size() == LOAD_LIMIT);
    return newGraph;
  }

  /**
   * This is a {@link Callable} that computes a new graph.
   */
  private class GraphConstruction implements Runnable {

    private long issuedTimestamp;

    private GraphConstruction(long issuedTimestamp) {
      this.issuedTimestamp = issuedTimestamp;
    }

    @Override
    public void run() {
      Graph newGraph = getGremlinGraphFromKnowledgeGraph();
      graphLock.writeLock().lock();
      try {
        Graph oldGraph;
        if (this.issuedTimestamp > AbstractClonedGremlinDAO.this.timestamp) {
          oldGraph = AbstractClonedGremlinDAO.this.graph;
          AbstractClonedGremlinDAO.this.graph = newGraph;
          AbstractClonedGremlinDAO.this.timestamp = this.issuedTimestamp;
          GremlinDAOUpdatedEvent gremlinDAOUpdatedEvent = new GremlinDAOUpdatedEvent(this);
          applicationEventPublisher.publishEvent(gremlinDAOUpdatedEvent);
          if (updateTriggerFunction != null) {
            updateTriggerFunction.accept(gremlinDAOUpdatedEvent);
          }
        } else {
          oldGraph = newGraph;
        }
        try {
          oldGraph.close();
        } catch (Exception e) {
          logger.error("Gremlin graph could not be closed correctly. {}", e.getMessage());
        }
      } finally {
        graphLock.writeLock().unlock();
      }
    }
  }

  /**
   * Sets a listener for {@link GremlinDAOUpdatedEvent}s. The given argument can be {@code null},
   * but then the previous set listener will be removed.
   *
   * @param listener that shall be called if the {@link KGGremlinDAO} has updated.
   */
  public void setUpdateListenerFunction(Consumer<GremlinDAOUpdatedEvent> listener) {
    this.updateTriggerFunction = listener;
  }

}
