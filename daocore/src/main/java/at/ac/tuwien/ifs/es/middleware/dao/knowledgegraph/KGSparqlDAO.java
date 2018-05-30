package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.*;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.QueryResult;

/**
 * An instance of this interface represents a SPARQL interface to a certain knowledge graph. This
 * DAO provides the ability to query the graph using SPARQL.
 * <p/>
 * Instances of this DAO go through a life cycle starting with an {@code initial} state. From this
 * {@code initial} state, the implementation of this DAO can go into the {@code ready} state, if
 * everything works fine and as intended. The transition from the {@code initial} state into the
 * {@code ready} state triggers a {@link SPARQLDAOReadyEvent}.
 * <p/>
 * During the lifetime of a {@code ready} DAO, changes can be made to the underlying database. Those
 * changes will transition this DAO into a {@code updating} state (leading to a {@link
 * SPARQLDAOUpdatingEvent}). If the updating is finalized, the DAO is {@code ready} again. This will
 * issue a {@link SPARQLDAOUpdatedEvent}.
 * <p/>
 * However, the DAO can always fail, and the resulting state will then be {@code failed}. A
 * transition into {@code failed}, will trigger a {@link SPARQLDAOFailedEvent}. A DAO can always
 * recover, and move to {@code ready} again, also triggering the corresponding event.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KGSparqlDAO {

  /**
   * Queries the knowledge graph using given SPARQL {@code query} and returns the result. The
   * operations SELECT, ASK, CONSTRUCT and DESCRIBE are supported by this method.
   *
   * @param query which shall be executed (SELECT, ASK, CONSTRUCT and DESCRIBE).
   * @param includeInferred {@code true}, if entailed statements should be considered, otherwise
   * {@code false}.
   * @return {@link QueryResult} of the SPARQL query.
   * @throws KnowledgeGraphSPARQLException if the given SPARQL query could not be executed
   * successfully.
   */
  QueryResult query(String query, boolean includeInferred) throws KnowledgeGraphSPARQLException;

  /**
   * Updates the knowledge graph using given SPARQL {@code query}.
   *
   * @param query which shall be executed (INSERT, DELETE).
   * @throws KnowledgeGraphSPARQLException if the given SPARQL query could not be executed
   * successfully.
   */
  void update(String query) throws KnowledgeGraphSPARQLException;
}
