package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLException;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.QueryResult;

/**
 * An instance of this interface represents a SPARQL interface to a certain knowledge graph. This
 * DAO provides the ability to query the graph using SPARQL.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KGSparqlDAO extends KGDAO {

  /**
   * Queries the knowledge graph using given SPARQL {@code query} and returns the result. The
   * operations SELECT, ASK, CONSTRUCT and DESCRIBE are supported by this method.
   *
   * @param query which shall be executed (SELECT, ASK, CONSTRUCT and DESCRIBE).
   * @param includeInferred {@code true}, if entailed statements should be considered, otherwise
   * {@code false}.
   * @return {@link QueryResult} of the SPARQL query.
   * @throws KGSPARQLException if the given SPARQL query could not be executed
   * successfully.
   */
  <T extends QueryResult> T query(String query, boolean includeInferred)
      throws KGSPARQLException;

  /**
   * Updates the knowledge graph using given SPARQL {@code query}.
   *
   * @param query which shall be executed (INSERT, DELETE).
   * @throws KGSPARQLException if the given SPARQL query could not be executed
   * successfully.
   */
  void update(String query) throws KGSPARQLException;

}
