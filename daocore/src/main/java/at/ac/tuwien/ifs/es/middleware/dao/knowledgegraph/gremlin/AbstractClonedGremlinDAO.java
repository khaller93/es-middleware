package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOStatusChangeListener;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.GremlinDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.SparqlDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLException;
import at.ac.tuwien.ifs.es.middleware.sparql.result.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOFailedStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOInitStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOReadyStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus.CODE;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOUpdatingStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.rdf.api.BlankNode;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
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

  private static final int STATEMENT_LOAD_LIMIT = 100000;

  private static final String ALL_STATEMENTS_QUERY = "SELECT ?s ?p ?o WHERE {\n"
      + "    ?s ?p ?o .\n"
      + "    FILTER(isIRI(?s) && isIRI(?o)) .\n"
      + "}\n"
      + "OFFSET ${offset}\n"
      + "LIMIT ${limit}";

  /* application context */
  private ApplicationContext context;
  /* thread pool for spanning syncing operations */
  private TaskExecutor taskExecutor;
  /* lock for controlling syncing operations */
  private Lock graphLock = new ReentrantLock();
  /* status of the this gremlin DAO */
  private KGDAOStatus status;
  /* change listener for status */
  private List<KGDAOStatusChangeListener> statusChangeListeners;
  /* SPARQL from which data shall be cloned */
  private KGSparqlDAO sparqlDAO;
  /* storing graph data */
  private Graph graph;
  /* schema for the graph data */
  private PGS schema;
  /* update lock */
  private Lock updateLock = new ReentrantLock();
  /* current timestamp for the most current, integrated data */
  private Instant currentTimestamp = Instant.MIN;
  /* Map maintaining the graph construction processes */
  private Map<Long, GraphConstruction> graphConstructionMap = new ConcurrentHashMap<>();

  @Value("${esm.db.gremlin.syncOnStart:#{false}}")
  private boolean syncOnStart;

  /**
   * Creates a new {@link AbstractClonedGremlinDAO} with the given {@code knowledgeGraphDAO}.
   *
   * @param context to publish the events.
   * @param sparqlDAO that shall be used.
   */
  public AbstractClonedGremlinDAO(ApplicationContext context, KGSparqlDAO sparqlDAO,
      PGS schema, TaskExecutor taskExecutor) {
    this.context = context;
    this.sparqlDAO = sparqlDAO;
    this.status = new KGDAOInitStatus();
    this.schema = schema;
    this.statusChangeListeners = new LinkedList<>();
    this.taskExecutor = taskExecutor;
  }

  @PostConstruct
  public void setUp() {
    if (CODE.READY.equals(sparqlDAO.getStatus().getCode())) {
      if (syncOnStart || !graph.vertices().hasNext()) {
        executeGraphConstruction(-1, Instant.now());
      } else {
        logger.info("No syncing of '{}' scheduled,", this.getClass().getSimpleName());
        setStatus(new KGDAOReadyStatus());
      }
    } else {
      logger.info("SPARQL DAO is not ready, so no syncing can take place.");
    }
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
  public void onSPARQLReadyEvent(SparqlDAOStateChangeEvent event) {
    KGDAOStatus status = event.getStatus();
    if (CODE.READY.equals(status.getCode())) {
      logger.debug("Recognized a SPARQL DAO state change event to READY ({} -> READY). {}",
          event.getPreviousStatus().getCode().name().toUpperCase(), event);
      updateLock.lock();
      try {
        if (event.getDAOTimestamp().isAfter(currentTimestamp)) {
          if (!graph.vertices().hasNext()) {
            logger.info("The graph database '{}' is empty.", this.getClass().getSimpleName());
            executeGraphConstruction(event);
          } else {
            if (CODE.INITIATING.equals(this.status.getCode())) {
              if (syncOnStart) {
                executeGraphConstruction(event);
              } else {
                logger.info(
                    "Graph db '{}' is not syncing with SPARQL DAO at start. Set 'esm.db.gremlin.syncOnStart' to true in the properties file in order to do so.");
                currentTimestamp = event.getDAOTimestamp();
              }
            } else {
              executeGraphConstruction(event);
            }
          }
        } else {
          logger.debug("Ignores the state update {}.", event);
        }
      } finally {
        updateLock.unlock();
      }
    } else {
      logger.debug("Recognized a SPARQL DAO state change event ({} -> {}). {}",
          event.getPreviousStatus().getCode().name().toUpperCase(),
          status.getCode().name().toUpperCase());
    }
  }

  private void executeGraphConstruction(SparqlDAOStateChangeEvent event) {
    executeGraphConstruction(event.getEventId(), event.getDAOTimestamp());
  }

  private void executeGraphConstruction(long eventId, Instant timestamp) {
    graphConstructionMap.compute(eventId,
        (aLong, graphConstruction) -> {
          if (graphConstruction != null) {
            return graphConstruction;
          } else {
            GraphConstruction gc = new GraphConstruction(eventId, timestamp);
            taskExecutor.execute(gc);
            return gc;
          }
        });
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

  public KGDAOStatus getStatus() {
    return status;
  }

  @Override
  public Features getFeatures() {
    return graph.features();
  }

  @Override
  public void addStatusChangeListener(KGDAOStatusChangeListener changeListener) {
    checkArgument(changeListener != null, "The change listener must not be null.");
    statusChangeListeners.add(changeListener);
  }

  protected synchronized void setStatus(KGDAOStatus status) {
    checkArgument(status != null, "The specified status must not be null.");
    if (!this.status.getCode().equals(status.getCode())) {
      KGDAOStatus prevStatus = this.status;
      this.status = status;
      context.publishEvent(new GremlinDAOStateChangeEvent(this, status, prevStatus,
          Instant.now()));
      statusChangeListeners.forEach(changeListener -> changeListener.onStatusChange(status));
    }
  }

  protected synchronized void setStatus(KGDAOStatus status, long eventId) {
    checkArgument(status != null, "The specified status must not be null.");
    if (!this.status.getCode().equals(status.getCode())) {
      KGDAOStatus prevStatus = this.status;
      this.status = status;
      context.publishEvent(new GremlinDAOStateChangeEvent(this, eventId, status, prevStatus,
          Instant.now()));
      statusChangeListeners.forEach(changeListener -> changeListener.onStatusChange(status));
    }
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

    private long eventId;
    private Instant issuedTimestamp;

    private GraphConstruction(long eventId, Instant issuedTimestamp) {
      checkArgument(issuedTimestamp != null, "Given timestamp must not be null.");
      this.eventId = eventId;
      this.issuedTimestamp = issuedTimestamp;
    }

    private Vertex prepareVertex(BlankNodeOrIRI resource, Map<BlankNodeOrIRI, Vertex> cache) {
      if (cache.containsKey(resource)) {
        return cache.get(resource);
      }
      String sIRI = resource instanceof IRI ? ((IRI) resource).getIRIString()
          : "_:" + ((BlankNode) resource).uniqueReference();
      Iterator<Vertex> vertexIt = graph.traversal().V()
          .has(schema.iri().identifierAsString(), sIRI);
      if (vertexIt.hasNext()) {
        Vertex v = vertexIt.next();
        v.property(Cardinality.single, "version", toTimeStampInNs(issuedTimestamp));
        cache.put(resource, v);
        return v;
      } else {
        Vertex v = graph
            .addVertex(schema.iri().identifier(), sIRI, schema.kind().identifier(), "iri",
                "version", toTimeStampInNs(issuedTimestamp));
        cache.put(resource, v);
        return v;
      }
    }

    private long toTimeStampInNs(Instant timestamp) {
      return timestamp.getEpochSecond() * 10 ^ 9 + timestamp.getNano();
    }

    @Override
    public void run() {
      logger.info("Starts to construct an '{}' graph with timestamp={} and transaction support={}.",
          AbstractClonedGremlinDAO.this.getClass().getSimpleName(),
          issuedTimestamp, areTransactionSupported());
      /* updating the gremlin status */
      setStatus(new KGDAOUpdatingStatus(), eventId);
      /* prepare the vertex map by fetching all resources (IRI) */
      Map<BlankNodeOrIRI, Vertex> vertexCacheMap = new HashMap<>();
      boolean successful = true;
      List<Map<String, RDFTerm>> values;
      int offset = 0;
      do {
        /* load statements from SPARQL DAO */
        try {
          Map<String, String> valuesMap = new HashMap<>();
          valuesMap.put("offset", String.valueOf(offset));
          valuesMap.put("limit", String.valueOf(STATEMENT_LOAD_LIMIT));
          values = sparqlDAO.<SelectQueryResult>query(
              new StringSubstitutor(valuesMap).replace(ALL_STATEMENTS_QUERY), true).value();
          AbstractClonedGremlinDAO.this.lock();
          /* import it into the graph database */
          try {
            for (Map<String, RDFTerm> row : values) {
              BlankNodeOrIRI property = (BlankNodeOrIRI) row.get("p");
              Vertex subjectVertex = prepareVertex((BlankNodeOrIRI) row.get("s"), vertexCacheMap);
              Vertex objectVertex = prepareVertex((BlankNodeOrIRI) row.get("o"), vertexCacheMap);
              subjectVertex
                  .addEdge(property instanceof IRI ? ((IRI) property).getIRIString()
                      : "_:" + ((BlankNode) property).uniqueReference(), objectVertex, "version",
                      toTimeStampInNs(issuedTimestamp));
            }
            logger
                .info("Loaded {} statements from the knowledge graph {}.", offset + values.size(),
                    sparqlDAO.getClass().getSimpleName());
            offset += STATEMENT_LOAD_LIMIT;
            AbstractClonedGremlinDAO.this.commit();
          } catch (Exception e) {
            logger.error("An exception occurred while loading the graph. {}", e.getMessage());
            setStatus(new KGDAOFailedStatus("Updating the Gremlin graph failed.", e), eventId);
            AbstractClonedGremlinDAO.this.rollback();
            successful = false;
            break;
          } finally {
            AbstractClonedGremlinDAO.this.unlock();
          }
        } catch (KGSPARQLException e) {
          logger.error("An exception occurred while loading the graph. {}", e.getMessage());
          setStatus(new KGDAOFailedStatus("Updating the Gremlin graph failed.", e), eventId);
          successful = false;
          break;
        }
      } while (!values.isEmpty() && values.size() == STATEMENT_LOAD_LIMIT
          && currentTimestamp.compareTo(issuedTimestamp) <= 0);
      if (successful) {
        logger.debug("An bulk load with timestamp '{}' has been committed.", issuedTimestamp);
        updateLock.lock();
        try {
          currentTimestamp = issuedTimestamp;
        } finally {
          updateLock.unlock();
        }
      }
      /* remove old/corrupt data */
      long cT = toTimeStampInNs(issuedTimestamp);
      AbstractClonedGremlinDAO.this.lock();
      try {
        if (successful) {
          graph.traversal().V().has("version", P.lt(cT)).drop().iterate();
          graph.traversal().E().has("version", P.lt(cT)).drop().iterate();
        } else {
          graph.traversal().V().has("version", P.eq(cT)).drop().iterate();
          graph.traversal().E().has("version", P.eq(cT)).drop().iterate();
        }
        AbstractClonedGremlinDAO.this.commit();
      } catch (Exception e) {
        e.printStackTrace();
        AbstractClonedGremlinDAO.this.rollback();
      } finally {
        AbstractClonedGremlinDAO.this.unlock();
      }
      AbstractClonedGremlinDAO.this.onBulkLoadCompleted();
      setStatus(new KGDAOReadyStatus(), eventId);
    }
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
