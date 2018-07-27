package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOFailedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatingEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOFailedStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOInitStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOReadyStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOUpdatingStatus;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

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

  private static final int LOAD_LIMIT = 10000;

  private static final String ALL_STATEMENTS_QUERY = "SELECT DISTINCT ?s ?p ?o WHERE {\n"
      + "  ?s ?p ?o .\n"
      + "}\n"
      + "OFFSET ${offset}\n"
      + "LIMIT ${limit}";

  /* application context */
  private ApplicationContext applicationContext;
  /* thread pool for spanning syncing operations */
  private ExecutorService threadPool = Executors.newCachedThreadPool();
  /* lock for controlling syncing operations */
  private Lock graphLock = new ReentrantLock();

  /* status of the this gremlin DAO */
  private KGDAOStatus status;
  /* SPARQL from which data shall be cloned */
  private KGSparqlDAO sparqlDAO;
  /* storing graph data */
  private Graph graph;
  /* current timestamp for the most current, integrated data */
  private AtomicLong currentTimestamp = new AtomicLong(0);
  /* timestamp of latest submitted changes of data */
  private AtomicLong newestDatatimestamp = new AtomicLong(0);

  private Consumer<ApplicationEvent> updatedListener;

  /**
   * Creates a new {@link AbstractClonedGremlinDAO} with the given {@code knowledgeGraphDAO}.
   *
   * @param applicationContext to publish the events.
   * @param sparqlDAO that shall be used.
   */
  public AbstractClonedGremlinDAO(ApplicationContext applicationContext, KGSparqlDAO sparqlDAO) {
    this.applicationContext = applicationContext;
    this.sparqlDAO = sparqlDAO;
    this.status = new KGDAOInitStatus();
  }

  /**
   * Sets the graph that shall be used for this gremlin dao.
   *
   * @param graph that shall be used for this gremlin dao.
   */
  protected void setGraph(Graph graph) {
    this.graph = graph;
  }

  @Override
  public Transaction getTransaction() {
    return graph.tx();
  }

  @Override
  public Lock getLock() {
    return graphLock;
  }

  @EventListener
  public void onSPARQLReadyEvent(SPARQLDAOReadyEvent event) {
    logger.debug("SPARQL DAO is ready {}.", event);
    /* only do initial construction if graph is empty */
    newestDatatimestamp.accumulateAndGet(event.getTimestamp(),
        (current, updated) -> current <= updated ? updated : current);
    if (!graph.vertices().hasNext()) {
      threadPool
          .submit(new GraphConstruction(event.getTimestamp(), new GremlinDAOReadyEvent(this)));
    }
  }

  @EventListener
  public void onSPARQLUpdatedEvent(SPARQLDAOUpdatedEvent event) {
    logger.debug("Recognized an SPARQL update event {}.", event);
    if (newestDatatimestamp.get() < event.getTimestamp()) {
      newestDatatimestamp.accumulateAndGet(event.getTimestamp(),
          (current, updated) -> current < updated ? updated : current);
      threadPool
          .submit(new GraphConstruction(event.getTimestamp(), new GremlinDAOUpdatedEvent(this)));
    }
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
   * This is a {@link Callable} that computes a new graph.
   */
  private class GraphConstruction implements Runnable {

    private long issuedTimestamp;
    private ApplicationEvent eventForSuccess;

    private GraphConstruction(long issuedTimestamp, ApplicationEvent eventForSuccess) {
      this.issuedTimestamp = issuedTimestamp;
      this.eventForSuccess = eventForSuccess;
    }

    public Vertex getVertex(Map<BlankNodeOrIRI, Vertex> cache, BlankNodeOrIRI node) {
      if (!cache.containsKey(node)) {
        String sIRI = BlankOrIRIJsonUtil.stringValue(node);
        Iterator<Vertex> vertexIt = Collections.<Vertex>emptyList().iterator();  //todo: reimplement
        if (vertexIt.hasNext()) {
          Vertex v = vertexIt.next();
          v.property(Cardinality.set, "version", issuedTimestamp);
          return v;
        } else {
          Vertex v = graph.addVertex(T.label, sIRI, "version", issuedTimestamp);
          cache.put(node, v);
          return v;
        }
      } else {
        return cache.get(node);
      }
    }

    @Override
    public void run() {
      logger.info("Starts to construct an '{}' graph with {}.",
          AbstractClonedGremlinDAO.this.getClass().getSimpleName(),
          issuedTimestamp);
      Map<BlankNodeOrIRI, Vertex> recognizedNodes = new HashMap<>();
      List<Map<String, RDFTerm>> values;
      boolean transactionSupported = getFeatures().graph().supportsTransactions();
      int offset = 0;
      if (transactionSupported) {
        graph.tx().open();
      } else {
        getLock().lock();
      }
      try {
        applicationContext
            .publishEvent(new GremlinDAOUpdatingEvent(AbstractClonedGremlinDAO.this));
        AbstractClonedGremlinDAO.this.status = new KGDAOUpdatingStatus();
        do {
          Map<String, String> valuesMap = new HashMap<>();
          valuesMap.put("offset", String.valueOf(offset));
          valuesMap.put("limit", String.valueOf(LOAD_LIMIT));
          values = ((SelectQueryResult) sparqlDAO
              .query(new StringSubstitutor(valuesMap).replace(ALL_STATEMENTS_QUERY), true)).value();
          for (Map<String, RDFTerm> row : values) {
            Vertex sVertex = null, oVertex = null;
            RDFTerm subject = row.get("s");
            if (subject instanceof IRI) {
              sVertex = getVertex(recognizedNodes, (IRI) row.get("s"));
            }
            RDFTerm object = row.get("o");
            if (object instanceof IRI) {
              oVertex = getVertex(recognizedNodes, (IRI) object);
            }
            if (sVertex != null && oVertex != null) {
              BlankNodeOrIRI property = (BlankNodeOrIRI) row.get("p");
              sVertex.addEdge(BlankOrIRIJsonUtil.stringValue(property), oVertex, "version",
                  issuedTimestamp);
            }
          }
          logger.info("Loaded {} statements from the knowledge graph {}.", offset + values.size(),
              sparqlDAO.getClass().getSimpleName());
          offset += LOAD_LIMIT;
        } while (!values.isEmpty() && values.size() == LOAD_LIMIT
            && currentTimestamp.get() < issuedTimestamp);
        if (currentTimestamp.get() < issuedTimestamp) {
          currentTimestamp.accumulateAndGet(issuedTimestamp,
              (current, updated) -> current <= updated ? updated : current);
          if (currentTimestamp.get() == newestDatatimestamp.get()) {
            applicationContext.publishEvent(eventForSuccess);
            AbstractClonedGremlinDAO.this.status = new KGDAOReadyStatus();
            if (updatedListener != null && (eventForSuccess instanceof GremlinDAOUpdatedEvent)) {
              updatedListener.accept(eventForSuccess);
            }
          }
          if (transactionSupported) {
            graph.tx().commit();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        logger.error("An exception occurred while loading the graph. {}", e.getMessage());
        applicationContext
            .publishEvent(new GremlinDAOFailedEvent(AbstractClonedGremlinDAO.this,
                "Updating the Gremlin graph failed.", e));
        AbstractClonedGremlinDAO.this.status = new KGDAOFailedStatus(
            "Updating the Gremlin graph failed.", e);
        if (transactionSupported) {
          graph.tx().rollback();
        }
      } finally {
        if (!transactionSupported) {
          getLock().unlock();
        }
        deleteOldData();
      }
    }

    private void deleteOldData() {
      boolean transactionSupported = getFeatures().graph().supportsTransactions();
      if (transactionSupported) {
        graph.tx().open();
      } else {
        getLock().lock();
      }
      long cT = currentTimestamp.get();
      try {
        graph.traversal().V().has("version", P.lt(cT)).drop().iterate();
        graph.traversal().E().has("version", P.lt(cT)).drop().iterate();
        if (transactionSupported) {
          graph.tx().commit();
        }
      } catch (Exception e) {
        if (transactionSupported) {
          graph.tx().rollback();
        }
      } finally {
        if (transactionSupported) {
          graph.tx().close();
        } else {
          getLock().unlock();
        }
      }
    }
  }

  public void setUpdatedListener(Consumer<ApplicationEvent> updatedListener) {
    this.updatedListener = updatedListener;
  }

  @PreDestroy
  public void tearDown() {
    try {
      if (graph != null) {
        graph.close();
      }
      threadPool.shutdown();
    } catch (Exception e) {
      logger.error("Failed to close the Gremlin graph DAO. {}", e.getMessage());
    }
  }

}
