package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatusChangeListener;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOInitStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
import org.springframework.context.ApplicationContext;
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

  @Override
  public void setup() {
    new GraphConstruction(Instant.now()).run();
  }

  @Override
  public void update(long timestamp) throws KGDAOException {
    new GraphConstruction(Instant.now()).run();
  }

  /**
   * Sets the graph that shall be used for this gremlin dao.
   *
   * @param graph that shall be used for this gremlin dao.
   */
  protected void setGraph(Graph graph) {
    this.graph = graph;
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

    private GraphConstruction(Instant issuedTimestamp) {
      checkArgument(issuedTimestamp != null, "Given timestamp must not be null.");
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
            AbstractClonedGremlinDAO.this.rollback();
            successful = false;
            break;
          } finally {
            AbstractClonedGremlinDAO.this.unlock();
          }
        } catch (KGSPARQLException e) {
          logger.error("An exception occurred while loading the graph. {}", e.getMessage());
          successful = false;
          break;
        }
      } while (!values.isEmpty() && values.size() == STATEMENT_LOAD_LIMIT);
      if (successful) {
        logger.debug("An bulk load with timestamp '{}' has been committed.", issuedTimestamp);
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
