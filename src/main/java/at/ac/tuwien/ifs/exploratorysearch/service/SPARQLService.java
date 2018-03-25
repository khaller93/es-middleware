package at.ac.tuwien.ifs.exploratorysearch.service;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.exploratorysearch.dto.sparql.QueryResult;

/**
 * This service provides methods for executing SPARQL queries and updates.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface SPARQLService {

  /**
   * Queries the knowledge graph using given SPARQL {@code query} and returns the result. Supported
   * are the constructs {@code ASK}, {@code SELECT}, {@code CONSTRUCT} and {@code DESCRIBE}. For
   * altering the knowledge graph with SPARQL, you have to use
   *
   * @param query which shall be executed.
   * @param includeInference {@code true}, if entailed statements should be considered, otherwise
   * {@code false}.
   * @return {@link QueryResult} of the SPARQL query.
   * @throws SPARQLExecutionException, if the execution of the SPARQL query failed.
   */
  QueryResult query(String query, boolean includeInference) throws SPARQLExecutionException;

  /**
   * Executes the given SPARQL {@code query}.
   *
   * @param query which shall be executed.
   * @throws SPARQLExecutionException if the execution of the SPARQL query failed.
   */
  void update(String query) throws SPARQLExecutionException;

}
