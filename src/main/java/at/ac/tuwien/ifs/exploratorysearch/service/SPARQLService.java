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
   * Queries the knowledge graph using given SPARQL {@code query} and returns the result.
   *
   * @param query which shall be executed.
   * @param includeInference {@code true}, if entailed statements should be considered, otherwise
   * {@code false}.
   * @return {@link QueryResult} of the SPARQL query.
   */
  QueryResult query(String query, boolean includeInference) throws SPARQLExecutionException;

}
