package at.ac.tuwien.ifs.es.middleware.dao.rdf4j;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOFailedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOUpdatedEvent;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
 * This class implements the generic methods of {@link KnowledgeGraphDAOConfig} using the RDF4J
 * framework.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="http://rdf4j.org/">RDF4J</a>
 * @since 1.0
 */
public abstract class RDF4JKnowledgeGraphDAO implements KGSparqlDAO {

  private static final Logger logger = LoggerFactory.getLogger(RDF4JKnowledgeGraphDAO.class);

  private Lock updateLock = new ReentrantLock();

  @Value("${esm.db.updateInterval:5000}")
  private long updateInterval;
  private Timer timer;

  private Repository repository;
  private ApplicationContext applicationContext;

  private KGDAOStatus status;

  public RDF4JKnowledgeGraphDAO(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    this.status = new KGDAOInitStatus();
  }

  /**
   * Initializes a new {@link RDF4JKnowledgeGraphDAO} with the given {@link Repository}.
   *
   * @param repository {@link Repository} that shall be initialized.
   */
  protected void init(Repository repository) {
    assert repository != null;
    this.repository = repository;
    try {
      this.repository.initialize();
    } catch (RepositoryException re) {
      applicationContext
          .publishEvent(
              new SPARQLDAOFailedEvent(this, "Initialization of triplestore failed.", re));
      status = new KGDAOFailedStatus("Initialization of triplestore failed.", re);
      throw re;
    }
    applicationContext.publishEvent(new SPARQLDAOReadyEvent(this));
    this.status = new KGDAOReadyStatus();
  }

  /**
   * Initializes a new {@link RDF4JKnowledgeGraphDAO} with the given {@link Sail}.
   *
   * @param sail {@link Sail} that shall be initialized.
   */
  protected void init(Sail sail) {
    this.init(new SailRepository(sail));
  }

  @Override
  public KGDAOStatus getSPARQLStatus() {
    return status;
  }

  /**
   * Gets the application context maintained by this DAO.
   *
   * @return {@link ApplicationContext} that is maintained by this DAO.
   */
  protected ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public QueryResult query(String queryString, boolean includeInferred)
      throws KnowledgeGraphSPARQLException {
    logger
        .debug("Query {} was requested to be executed. Inference={}", queryString, includeInferred);
    try (RepositoryConnection con = repository.getConnection()) {
      Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString);
      query.setIncludeInferred(includeInferred);
      if (query instanceof TupleQuery) {
        TupleQueryResult result = ((TupleQuery) query).evaluate();
        return new RDF4JSelectQueryResult(result.getBindingNames(), QueryResults.asList(result));
      } else if (query instanceof BooleanQuery) {
        return new RDF4JAskQueryResult(((BooleanQuery) query).evaluate());
      } else if (query instanceof GraphQuery) {
        GraphQueryResult graphQueryResult = ((GraphQuery) query).evaluate();
        return new RDF4JGraphQueryResult(graphQueryResult.getNamespaces(),
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
    logger.debug("Update {} was requested to be executed", query);
    try (RepositoryConnection con = repository.getConnection()) {
      con.prepareUpdate(query).execute();
      updateLock.lock();
      try {
        if (timer == null) {
          timer = new Timer();
          logger.debug("Scheduled a new update task for time {}.", updateInterval);
          timer.schedule(new TimerTask() {
            @Override
            public void run() {
              updateLock.lock();
              try {
                logger.debug("The scheduled update task has been executed.");
                applicationContext.publishEvent(new SPARQLDAOUpdatedEvent(this));
                timer = null;
              } finally {
                updateLock.unlock();
              }
            }
          }, updateInterval);
        }
      } finally {
        updateLock.unlock();
      }
    } catch (RDF4JException e) {
      throw new SPARQLExecutionException(e);
    }
  }

  /**
   * Gets the repository used by this {@link RDF4JKnowledgeGraphDAO}.
   *
   * @return the repository used by this {@link RDF4JKnowledgeGraphDAO}.
   */
  public Repository getRepository() {
    return repository;
  }
}
