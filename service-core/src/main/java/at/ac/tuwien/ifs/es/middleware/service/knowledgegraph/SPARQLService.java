package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.QueryResult;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.exception.SPARQLServiceExecutionException;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.exception.SPARQLServiceIllegalArgumentException;

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
   * @return {@link QueryResult} get the SPARQL query.
   * @throws SPARQLServiceExecutionException will be thrown, if servicing the SPARQL query failed
   * (because of server).
   * @throws SPARQLServiceIllegalArgumentException will be thrown, if the given {@code query} is
   * malformed.
   */
  <T extends QueryResult> T query(String query, boolean includeInference)
      throws SPARQLServiceExecutionException, SPARQLServiceIllegalArgumentException;

  /**
   * Executes the given SPARQL {@code query}.
   *
   * @param query which shall be executed.
   * @throws SPARQLServiceExecutionException will be thrown, if servicing the SPARQL query failed
   * (because of server).
   * @throws SPARQLServiceIllegalArgumentException will be thrown, if the given {@code query} is
   * malformed.
   */
  void update(String query)
      throws SPARQLServiceExecutionException, SPARQLServiceIllegalArgumentException;

}
