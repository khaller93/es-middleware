package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql;


import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLExecutionException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGMalformedSPARQLQueryException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.sparql.QueryResult;

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
   * @throws KGSPARQLExecutionException will be thrown, if servicing the SPARQL query failed (because
   * get server).
   * @throws KGMalformedSPARQLQueryException will be thrown, if the given {@code query} is malformed.
   */
  <T extends QueryResult> T query(String query, boolean includeInference)
      throws KGSPARQLException;

  /**
   * Executes the given SPARQL {@code query}.
   *
   * @param query which shall be executed.
   * @throws KGSPARQLExecutionException will be thrown, if servicing the SPARQL query failed (because
   * get server).
   * @throws KGMalformedSPARQLQueryException will be thrown, if the given {@code query} is malformed.
   */
  void update(String query) throws KGSPARQLException;

}
