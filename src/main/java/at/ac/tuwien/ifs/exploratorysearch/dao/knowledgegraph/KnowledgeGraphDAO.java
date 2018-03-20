package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.exploratorysearch.dto.sparql.QueryResult;
import java.util.List;
import org.eclipse.rdf4j.repository.Repository;

/**
 * An instance of this interface represents a DAO to a certain knowledge graph. This DAO provides
 * the ability to query the graph using SPARQL. Furthermore, full-text-searches can be applied.
 * <p/>
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KnowledgeGraphDAO {

  /**
   * Queries the knowledge graph using given SPARQL {@code query} and returns the result.
   *
   * @param query which shall be executed.
   * @param includeInferred {@code true}, if entailed statements should be considered, otherwise
   * {@code false}.
   * @return {@link QueryResult} of the SPARQL query.
   * @throws SPARQLExecutionException if the given SPARQL query could not be executed.
   */
  QueryResult query(String query, boolean includeInferred) throws SPARQLExecutionException;

  Object update(String query);

  /**
   * Returns the RDF4J {@link Repository} that can be used to interact with the triplestore in which
   * the knowledge graph is managed.
   *
   * @return {@link Repository}, and must not be null.
   */
  Repository getRepository();

  /**
   * Applies a full text search on the managed knowledge graph using the given {@code keyword}.
   * <p/>
   * With the given {@code selectionQuery} a subset of resources can be specified that should be
   * considered for the full-text search. The binding name of the resources to consider should be
   * {@code ?s}. If {@code selectionQuery} is {@code null}, all resources will be considered.
   * <p/>
   * This method returns a unbounded number of resources. Use {@link KnowledgeGraphDAO#searchFullText(String,
   * String, Long, Long)} for limiting the number of returned resources.
   *
   * @param selectionQuery {@code null}, or a query selecting for resources to consider.
   * @param keyword which shall be used to explore resources.
   * @return a ranked list of distinct resource IRIs.
   */
  default List<String> searchFullText(String selectionQuery, String keyword) {
    return searchFullText(selectionQuery, keyword, null, null);
  }

  /**
   * Applies a full text search on the managed knowledge graph using the given {@code keyword}.
   * <p/>
   * With the given {@code selectionQuery} a subset of resources can be specified that should be
   * considered for the full-text search. The binding name of the resources to consider should be
   * {@code ?s}. If {@code selectionQuery} is {@code null}, all resources will be considered.
   *
   * @param selectionQuery {@code null}, or a query selecting for resources to consider.
   * @param keyword which shall be used to explore resources.
   * @param limit the maximum number of returned resources.
   * @param offset a offset to moving the window of returned resources.
   * @return a ranked list of distinct resource IRIs.
   */
  List<String> searchFullText(String selectionQuery, String keyword, Long limit, Long offset);
}
