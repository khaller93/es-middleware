package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.util.RDF4JGraphQueryResult;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.util.RDF4JSelectQueryResult;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.util.RDF4JAskQueryResult;
import at.ac.tuwien.ifs.exploratorysearch.dto.sparql.QueryResult;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
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
  }

  @Override
  public QueryResult query(String queryString, boolean includeInferred)
      throws SPARQLExecutionException {
    try (RepositoryConnection con = repository.getConnection()) {
      Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString);
      query.setIncludeInferred(includeInferred);
      try {
        if (query instanceof TupleQuery) {
          return new RDF4JSelectQueryResult(((TupleQuery) query).evaluate());
        } else if (query instanceof BooleanQuery) {
          return new RDF4JAskQueryResult(((BooleanQuery) query).evaluate());
        } else if (query instanceof GraphQuery) {
          return new RDF4JGraphQueryResult(((GraphQuery) query).evaluate());
        } else {
          throw new IllegalArgumentException(String
              .format("Given query must be a SELECT, ASK or CONSTRUCT query, but was '%s'.",
                  query));
        }
      } catch (RDF4JException e) {
        throw new SPARQLExecutionException(e);
      }
    }
  }

  @Override
  public Object update(String query) {
    //TODO: Implement
    return null;
  }

}