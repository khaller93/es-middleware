package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOFailedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatingEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOFailedStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOInitStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOReadyStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOUpdatingStatus;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javax.annotation.PreDestroy;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;

/**
 * This is a {@link KGGremlinDAO} in which data will be cloned from the {@link KGSparqlDAO}. It
 * implements generic methods and expects simply a new clean {@link Graph} from the implementing
 * class to work.
 * <p/>
 * This implementation ignores blank nodes in the knowledge graph.
 * <p/>
 * In order to use this DAO, the graph to use must be set by the implementing class by calling the
 * method {@link AbstractClonedGremlinDAO#setGraph(Graph)}. This is best done in the constructor of
 * the implementing class.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractClonedGremlinDAO implements SPARQLSyncingGremlinDAO {

  private static final Logger logger = LoggerFactory.getLogger(AbstractClonedGremlinDAO.class);

  private static final int LOAD_LIMIT = 100000;

  private static final String ALL_RESOURCE_IRIS_QUERY = "SELECT DISTINCT ?resource WHERE {\n"
      + "    {?resource ?p1 _:o1}\n"
      + "     UNION\n"
      + "    {\n"
      + "        _:o2 ?p2 ?resource .\n"
      + "        FILTER (isIRI(?resource)) .\n"
      + "    } \n"
      + "}\n"
      + "OFFSET ${offset}\n"
      + "LIMIT ${limit}";

  private static final String ALL_STATEMENTS_QUERY = "SELECT ?s ?p ?o WHERE {\n"
      + "    ?s ?p ?o .\n"
      + "    FILTER(isIRI(?s) && isIRI(?o)) .\n"
      + "}\n"
      + "OFFSET ${offset}\n"
      + "LIMIT ${limit}";

  /* application context */
  private ApplicationContext applicationContext;
  /* thread pool for spanning syncing operations */
  private TaskExecutor taskExecutor;
  /* lock for controlling syncing operations */
  private Lock graphLock = new ReentrantLock();

  /* status of the this gremlin DAO */
  private KGDAOStatus status;
  /* SPARQL from which data shall be cloned */
  private KGSparqlDAO sparqlDAO;
  /* storing graph data */
  private Graph graph;
  /* schema for the graph data */
  private PGS schema;
  /* update lock */
  private Lock updateLock = new ReentrantLock();
  /* current timestamp for the most current, integrated data */
  //private AtomicLong currentTimestamp = new AtomicLong(0);
  private Instant currentTimestamp = Instant.now();
  /* timestamp of latest submitted changes of data */
  //private AtomicLong newestDatatimestamp = new AtomicLong(0);
  private Instant newestUpdateTimestamp;

  private Consumer<ApplicationEvent> updatedListener;

  /**
   * Creates a new {@link AbstractClonedGremlinDAO} with the given {@code knowledgeGraphDAO}.
   *
   * @param applicationContext to publish the events.
   * @param sparqlDAO that shall be used.
   */
  public AbstractClonedGremlinDAO(ApplicationContext applicationContext, KGSparqlDAO sparqlDAO,
      PGS schema, TaskExecutor taskExecutor) {
    this.applicationContext = applicationContext;
    this.sparqlDAO = sparqlDAO;
    this.status = new KGDAOInitStatus();
    this.schema = schema;
    this.taskExecutor = taskExecutor;
  }

  /**
   * Sets the graph that shall be used for this gremlin dao.
   *
   * @param graph that shall be used for this gremlin dao.
   */
  protected void setGraph(Graph graph) {
    this.graph = graph;
  }

  @EventListener
  public void onSPARQLReadyEvent(SPARQLDAOReadyEvent event) {
    logger.debug("Recognized a SPARQL DAO ready event {}.", event);
    updateLock.lock();
    try {
      if (event.getDAOTimestamp().isAfter(currentTimestamp)) {
        if (!graph.vertices().hasNext()) {
          taskExecutor
              .execute(
                  new GraphConstruction(event.getDAOTimestamp(), new GremlinDAOReadyEvent(this)));
        } else {
          currentTimestamp = event.getDAOTimestamp();
          logger.debug("Readiness of SPARQL DAO triggers no update.");
        }
      }
    } finally {
      updateLock.unlock();
    }
  }

  @EventListener
  public void onSPARQLUpdatedEvent(SPARQLDAOUpdatedEvent event) {
    logger.debug("Recognized a SPARQL update event {}.", event);
    updateLock.lock();
    try {
      if (newestUpdateTimestamp == null || newestUpdateTimestamp
          .isBefore(event.getDAOTimestamp())) {
        newestUpdateTimestamp = event.getDAOTimestamp();
        taskExecutor
            .execute(
                new GraphConstruction(event.getDAOTimestamp(), new GremlinDAOUpdatedEvent(this)));
      }
    } finally {
      updateLock.unlock();
    }
  }

  protected abstract boolean areTransactionSupported();

  @Override
  public PGS getPropertyGraphSchema() {
    return schema;
  }

  @Override
  public GraphTraversalSource traversal() {
    return graph.traversal();
  }

  @Override
  public KGDAOStatus getGremlinStatus() {
    return status;
  }

  @Override
  public Features getFeatures() {
    return graph.features();
  }

  /**
   * This method is called, when potentially new data has been integrated into the graph.
   */
  protected void onBulkLoadCompleted() {

  }

  /**
   * This is a {@link Callable} that computes a new graph.
   */
  private class GraphConstruction implements Runnable {

    private Instant issuedTimestamp;
    private ApplicationEvent eventForSuccess;

    private GraphConstruction(Instant issuedTimestamp, ApplicationEvent eventForSuccess) {
      checkArgument(issuedTimestamp != null && eventForSuccess != null,
          "Given timestamp and event must not be null.");
      this.issuedTimestamp = issuedTimestamp;
      this.eventForSuccess = eventForSuccess;
    }

    /**
     * Fetches all the resources (IRI) ignoring blank nodes and assigns a vertex to each of them.
     *
     * @return the map of vertices covering all known resources (IRI).
     */
    private Map<BlankNodeOrIRI, Vertex> fetchVertices() {
      Map<BlankNodeOrIRI, Vertex> cache = new HashMap<>();
      AbstractClonedGremlinDAO.this.lock();
      try {
        List<Map<String, RDFTerm>> values;
        int offset = 0;
        do {
          Map<String, String> valuesMap = new HashMap<>();
          valuesMap.put("offset", String.valueOf(offset));
          valuesMap.put("limit", String.valueOf(LOAD_LIMIT));
          values = sparqlDAO.<SelectQueryResult>query(
              new StringSubstitutor(valuesMap).replace(ALL_RESOURCE_IRIS_QUERY), true)
              .value();
          if (values != null) {
            for (Map<String, RDFTerm> row : values) {
              BlankNodeOrIRI node = (IRI) row.get("resource");
              String sIRI = BlankOrIRIJsonUtil.stringValue(node);
              Iterator<Vertex> vertexIt = graph.traversal().V()
                  .has(schema.iri().identifierAsString(), sIRI);
              if (vertexIt.hasNext()) {
                Vertex v = vertexIt.next();
                v.property(Cardinality.single, "version", Date.from(issuedTimestamp));
                cache.put(node, v);
              } else {
                Vertex v = graph
                    .addVertex(schema.iri().identifier(), sIRI, schema.kind().identifier(), "iri",
                        "version", Date.from(issuedTimestamp));
                cache.put(node, v);
              }
            }
            offset += LOAD_LIMIT;
          } else {
            break;
          }
        } while (!values.isEmpty() && values.size() == LOAD_LIMIT
            && currentTimestamp.compareTo(issuedTimestamp) <= 0);
        AbstractClonedGremlinDAO.this.commit();
        return cache;
      } catch (Exception e) {
        AbstractClonedGremlinDAO.this.rollback();
        throw e;
      } finally {
        AbstractClonedGremlinDAO.this.unlock();
      }
    }

    @Override
    public void run() {
      logger.info("Starts to construct an '{}' graph with timestamp={} and transaction support={}.",
          AbstractClonedGremlinDAO.this.getClass().getSimpleName(),
          issuedTimestamp, areTransactionSupported());
      /* updating the gremlin status */
      applicationContext
          .publishEvent(new GremlinDAOUpdatingEvent(AbstractClonedGremlinDAO.this));
      AbstractClonedGremlinDAO.this.status = new KGDAOUpdatingStatus();
      /* prepare the vertex map by fetching all resources (IRI) */
      Map<BlankNodeOrIRI, Vertex> vertexMap = fetchVertices();
      AbstractClonedGremlinDAO.this.lock();
      try {
        List<Map<String, RDFTerm>> values;
        int offset = 0;
        do {
          Map<String, String> valuesMap = new HashMap<>();
          valuesMap.put("offset", String.valueOf(offset));
          valuesMap.put("limit", String.valueOf(LOAD_LIMIT));
          values = sparqlDAO.<SelectQueryResult>query(
              new StringSubstitutor(valuesMap).replace(ALL_STATEMENTS_QUERY), true).value();
          for (Map<String, RDFTerm> row : values) {
            BlankNodeOrIRI subject = (BlankNodeOrIRI) row.get("s");
            BlankNodeOrIRI property = (BlankNodeOrIRI) row.get("p");
            BlankNodeOrIRI object = (BlankNodeOrIRI) row.get("o");
            if (!vertexMap.containsKey(subject)) {
              logger.warn(
                  "The edge for '<{}> <{}> <{}>' could not be created, '{}' is missing in the vertex map.",
                  subject, property, object, subject);
              continue;
            }
            Vertex subjectVertex = vertexMap.get(subject);
            if (!vertexMap.containsKey(object)) {
              logger.warn(
                  "The edge for '<{}> <{}> <{}>' could not be created, '{}' is missing in the vertex map.",
                  subject, property, object, object);
            }
            Vertex objectVertex = vertexMap.get(object);
            subjectVertex.addEdge(BlankOrIRIJsonUtil.stringValue(property), objectVertex, "version",
                issuedTimestamp);
          }
          logger.info("Loaded {} statements from the knowledge graph {}.", offset + values.size(),
              sparqlDAO.getClass().getSimpleName());
          offset += LOAD_LIMIT;
        } while (!values.isEmpty() && values.size() == LOAD_LIMIT
            && currentTimestamp.compareTo(issuedTimestamp) <= 0);
        /* check status */
        boolean commit = false;
        updateLock.lock();
        try {
          if (currentTimestamp.isBefore(issuedTimestamp)) {
            if (issuedTimestamp.compareTo(newestUpdateTimestamp) == 0) {
              AbstractClonedGremlinDAO.this.status = new KGDAOReadyStatus();
              if (updatedListener != null && (eventForSuccess instanceof GremlinDAOUpdatedEvent)) {
                updatedListener.accept(eventForSuccess);
              }
            }
            commit = true;
            currentTimestamp = issuedTimestamp;
          }
        } finally {
          updateLock.unlock();
        }
        /* commit/rollback update */
        if (commit) {
          Date cT = Date.from(currentTimestamp);
          graph.traversal().V().has("version", P.lt(cT)).drop().iterate();
          graph.traversal().E().has("version", P.lt(cT)).drop().iterate();
          AbstractClonedGremlinDAO.this.commit();
          AbstractClonedGremlinDAO.this.onBulkLoadCompleted();
          applicationContext.publishEvent(eventForSuccess);
          logger.debug("An update with timestamp '{}' has been committed.", issuedTimestamp);
        } else {
          logger.debug("An update with timestamp '{}' is rolled back.", issuedTimestamp);
          AbstractClonedGremlinDAO.this.rollback();
        }
      } catch (Exception e) {
        logger.error("An exception occurred while loading the graph. {}", e.getMessage());
        applicationContext
            .publishEvent(new GremlinDAOFailedEvent(AbstractClonedGremlinDAO.this,
                "Updating the Gremlin graph failed.", e));
        AbstractClonedGremlinDAO.this.status = new KGDAOFailedStatus(
            "Updating the Gremlin graph failed.", e);
        AbstractClonedGremlinDAO.this.rollback();
      } finally {
        AbstractClonedGremlinDAO.this.unlock();
      }
    }
  }

  public void setUpdatedListener(Consumer<ApplicationEvent> updatedListener) {
    this.updatedListener = updatedListener;
  }

  @Override
  public void lock() {
    if (areTransactionSupported()) {
      graph.tx().open();
    } else {
      graphLock.lock();
    }
  }

  @Override
  public void commit() {
    if (areTransactionSupported()) {
      graph.tx().commit();
    }
  }

  @Override
  public void rollback() {
    if (areTransactionSupported()) {
      graph.tx().rollback();
    }
  }

  @Override
  public void unlock() {
    if (areTransactionSupported()) {
      if (graph.tx().isOpen()) {
        graph.tx().close();
      }
    } else {
      graphLock.unlock();
    }
  }

  @PreDestroy
  public void tearDown() {
    try {
      if (graph != null) {
        graph.close();
      }
    } catch (Exception e) {
      logger.error("Failed to close the Gremlin graph DAO. {}", e.getMessage());
    }
  }

}
