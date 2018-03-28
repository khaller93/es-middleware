package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.MalformedSPARQLQueryException;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.util.RDF4JGraphQueryResult;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.util.RDF4JSelectQueryResult;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.util.RDF4JAskQueryResult;
import at.ac.tuwien.ifs.exploratorysearch.dto.sparql.QueryResult;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the generic methods of {@link KnowledgeGraphDAO}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractKnowledgeGraphDAO implements KnowledgeGraphDAO {

  private static final Logger logger = LoggerFactory.getLogger(GraphDbDAO.class);

  private Repository repository;

  public AbstractKnowledgeGraphDAO(Repository repository) {
    assert repository != null;
    this.repository = repository;
    this.repository.initialize();
    afterInitialized(this.repository);
  }

  /**
   * This method will be called directly after the knowledgegraph repository has been initialized.
   *
   * @param repository the initialized {@link Repository}.
   */
  protected void afterInitialized(Repository repository) {

  }

  @Override
  public QueryResult query(String queryString, boolean includeInferred)
      throws KnowledgeGraphSPARQLException {
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
    try (RepositoryConnection con = repository.getConnection()) {
      con.prepareUpdate(query).execute();
    } catch (RDF4JException e) {
      throw new SPARQLExecutionException(e);
    }
  }

  @Override
  public Repository getRepository() {
    return repository;
  }
}
