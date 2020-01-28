package at.ac.tuwien.ifs.es.middleware.dao.rdf4j;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.KGUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOConnectionException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOSetupException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLExecutionException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGMalformedSPARQLQueryException;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.util.RDF4JAskQueryResult;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.util.RDF4JGraphQueryResult;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.util.RDF4JSelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.QueryResult;
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
import org.springframework.beans.factory.annotation.Autowired;
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

  private Repository repository;

  @Autowired
  private ApplicationContext context;

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
    } catch (RepositoryException e) {
      throw e;
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
  public void setup() throws KGDAOSetupException, KGDAOConnectionException {
    this.query("SELECT * WHERE {?s ?p ?o} LIMIT 5", true);
  }

  @Override
  public void update(long timestamp) throws KGDAOException {
    //nothing to do.
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends QueryResult> T query(String queryString, boolean includeInferred)
      throws KGSPARQLException {
    logger.trace("SPARQL Query {} was issued. Inference={}",
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
        throw new KGMalformedSPARQLQueryException(String
            .format(
                "Given query must be a SELECT, ASK or CONSTRUCT query, but was '%s'. For update queries use the corresponding endpoint.",
                query));
      }
    } catch (MalformedQueryException e) {
      throw new KGMalformedSPARQLQueryException(e);
    } catch (RDF4JException e) {
      throw new KGSPARQLExecutionException(e);
    }
  }

  @Override
  public void update(String query) throws KGSPARQLException {
    checkArgument(query != null && !query.isEmpty(),
        "The given query string must be specified and not be null or empty.");
    logger.trace("Update {} was requested to be executed", query.replaceAll("\\n", "\\\\n"));
    try (RepositoryConnection con = repository.getConnection()) {
      con.prepareUpdate(query).execute();
      context.publishEvent(new KGUpdatedEvent(this));
    } catch (RDF4JException e) {
      throw new KGSPARQLExecutionException(e);
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
}
