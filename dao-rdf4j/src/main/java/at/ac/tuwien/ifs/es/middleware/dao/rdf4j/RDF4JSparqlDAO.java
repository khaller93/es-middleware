package at.ac.tuwien.ifs.es.middleware.dao.rdf4j;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOStatusChangeListener;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.SparqlDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.MalformedSPARQLQueryException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.util.RDF4JAskQueryResult;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.util.RDF4JGraphQueryResult;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.util.RDF4JSelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.QueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOFailedStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOInitStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOReadyStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOUpdatingStatus;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PreDestroy;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.Sail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

/**
 * This class implements {@link KGSparqlDAO} using the RDF4J framework. It requires from the
 * implementing class to pass the {@link Repository} or {@link Sail} representing the connection
 * information to the triplestore.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="http://rdf4j.org/">RDF4J</a>
 * @since 1.0
 */
public abstract class RDF4JSparqlDAO implements KGSparqlDAO, AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(RDF4JSparqlDAO.class);

  @Value("${esm.db.updateinterval:15000}")
  private long updateInterval;
  @Value("${esm.db.updateinterval.timeout:60000}")
  private long updateIntervalTimout;
  private NotificationScheduler notificationScheduler;

  private Repository repository;
  private ApplicationContext applicationContext;

  private KGDAOStatus status;
  private List<KGDAOStatusChangeListener> statusChangeListenerList = new LinkedList<>();

  public RDF4JSparqlDAO(ApplicationContext applicationContext) {
    checkArgument(applicationContext != null, "The passed application context must not be null.");
    this.applicationContext = applicationContext;
    this.notificationScheduler = new NotificationScheduler(updateInterval, updateIntervalTimout);
    this.status = new KGDAOInitStatus();
  }

  /**
   * Initializes a new {@link RDF4JSparqlDAO} with the given {@link Repository}.
   *
   * @param repository {@link Repository} that shall be initialized.
   */
  protected void init(Repository repository) {
    checkArgument(repository != null, "The given repository msut not be null.");
    this.repository = repository;
    try {
      this.repository.init();
      setStatus(new KGDAOReadyStatus());
    } catch (RepositoryException re) {
      setStatus(new KGDAOFailedStatus("Initialization of triplestore failed.", re));
      throw re;
    }
  }

  /**
   * Initializes a new {@link RDF4JSparqlDAO} with the given {@link Sail}.
   *
   * @param sail {@link Sail} that shall be initialized.
   */
  protected void init(Sail sail) {
    this.init(new SailRepository(sail));
  }

  @Override
  public void addStatusChangeListener(KGDAOStatusChangeListener changeListener) {
    checkArgument(changeListener != null, "The given change listener must not be null.");
    statusChangeListenerList.add(changeListener);
  }

  @Override
  public KGDAOStatus getStatus() {
    return status;
  }


  protected synchronized void setStatus(KGDAOStatus status) {
    checkArgument(status != null, "The specified status must not be null.");
    if (!this.status.getCode().equals(status.getCode())) {
      KGDAOStatus prevStatus = this.status;
      this.status = status;
      applicationContext.publishEvent(new SparqlDAOStateChangeEvent(this, status, prevStatus,
          Instant.now()));
      statusChangeListenerList.forEach(changeListener -> changeListener.onStatusChange(status));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends QueryResult> T query(String queryString, boolean includeInferred)
      throws KnowledgeGraphSPARQLException {
    logger
        .trace("Query {} was requested to be executed. Inference={}",
            queryString.replaceAll("\\n", "\\\\n"), includeInferred);
    try (RepositoryConnection con = repository.getConnection()) {
      Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString);
      query.setIncludeInferred(includeInferred);
      if (query instanceof TupleQuery) {
        TupleQueryResult result = ((TupleQuery) query).evaluate();
        return (T) new RDF4JSelectQueryResult(result.getBindingNames(),
            QueryResults.asList(result));
      } else if (query instanceof BooleanQuery) {
        return (T) new RDF4JAskQueryResult(((BooleanQuery) query).evaluate());
      } else if (query instanceof GraphQuery) {
        GraphQueryResult graphQueryResult = ((GraphQuery) query).evaluate();
        return (T) new RDF4JGraphQueryResult(graphQueryResult.getNamespaces(),
            QueryResults.asList(graphQueryResult));
      } else {
        throw new MalformedSPARQLQueryException(String
            .format(
                "Given query must be a SELECT, ASK or CONSTRUCT query, but was '%s'. For update queries use the corresponding endpoint.",
                query));
      }
    } catch (MalformedQueryException e) {
      throw new MalformedSPARQLQueryException(e);
    } catch (RDF4JException e) {
      throw new SPARQLExecutionException(e);
    }
  }

  @Override
  public void update(String query) throws KnowledgeGraphSPARQLException {
    checkArgument(query != null && !query.isEmpty(),
        "The given query string must be specified and not be null or empty.");
    logger.trace("Update {} was requested to be executed", query.replaceAll("\\n", "\\\\n"));
    try (RepositoryConnection con = repository.getConnection()) {
      con.prepareUpdate(query).execute();
      notificationScheduler.updated();
    } catch (RDF4JException e) {
      throw new SPARQLExecutionException(e);
    }
  }

  /**
   * Gets the repository used by this {@link RDF4JSparqlDAO}.
   *
   * @return the repository used by this {@link RDF4JSparqlDAO}.
   */
  public Repository getRepository() {
    return repository;
  }


  @PreDestroy
  @Override
  public void close() throws Exception {
    if (repository != null) {
      repository.shutDown();
    }
  }

  /**
   * This class represents an util for handling {@link SparqlDAOStateChangeEvent}.
   *
   * @author Kevin Haller
   * @version 1.0
   * @since 1.0
   */
  class NotificationScheduler {

    private Timer timer = new Timer();
    private Lock notifyLock = new ReentrantLock();

    private long updateInterval;
    private long updateIntervalTimeout;

    private NotificationTask notificationTask;

    NotificationScheduler(long updateInterval, long updateIntervalTimeout) {
      this.updateInterval = updateInterval;
      this.updateIntervalTimeout = updateIntervalTimeout;
    }

    /**
     * This method shall be called if an update should be recognized.
     */
    public void updated() {
      notifyLock.lock();
      try {
        if (notificationTask == null) {
          setStatus(new KGDAOUpdatingStatus());
          notificationTask = new NotificationTask(Instant.now());
          timer.schedule(notificationTask, updateInterval);
        } else {
          notificationTask.update(Instant.now());
        }
      } finally {
        notifyLock.unlock();
      }
    }

    private final class NotificationTask extends TimerTask {

      private Instant initTimestamp;
      private Instant lastCallTimestamp;

      public NotificationTask(Instant initTimestamp) {
        this.initTimestamp = initTimestamp;
      }

      public void update(Instant now) {
        lastCallTimestamp = now;
      }

      @Override
      public void run() {
        Instant now = Instant.now();
        if (initTimestamp.plusMillis(updateIntervalTimeout).isBefore(now)) {
          publishUpdate(now);
        } else if (lastCallTimestamp == null) {
          publishUpdate(now);
        } else {
          timer.schedule(this, Date.from(lastCallTimestamp.plusMillis(updateInterval)));
        }
      }
    }

    private void publishUpdate(Instant now) {
      notifyLock.lock();
      try {
        logger.debug("An update event of the SPARQL DAO is published for timestamp {}.", now);
        setStatus(new KGDAOReadyStatus());
        notificationTask = null;
      } finally {
        notifyLock.unlock();
      }
    }

  }
}
