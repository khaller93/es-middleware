package at.ac.tuwien.ifs.exploratorysearch.service;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.MalformedSPARQLQueryException;
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
   * @throws SPARQLExecutionException will be thrown, if servicing the SPARQL query failed (because
   * of server).
   * @throws MalformedSPARQLQueryException will be thrown, if the given {@code query} is malformed.
   */
  QueryResult query(String query, boolean includeInference) throws KnowledgeGraphSPARQLException;

  /**
   * Executes the given SPARQL {@code query}.
   *
   * @param query which shall be executed.
   * @throws SPARQLExecutionException will be thrown, if servicing the SPARQL query failed (because
   * of server).
   * @throws MalformedSPARQLQueryException will be thrown, if the given {@code query} is malformed.
   */
  void update(String query) throws KnowledgeGraphSPARQLException;

}
