package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.exploratorysearch.dto.sparql.QueryResult;
import org.eclipse.rdf4j.repository.Repository;

/**
 * An instance of this interface represents a DAO to a certain knowledge graph. This DAO provides
 * the ability to query the graph using SPARQL.
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
   * @throws KnowledgeGraphSPARQLException if the given SPARQL query could not be executed
   * successfully.
   */
  QueryResult query(String query, boolean includeInferred) throws KnowledgeGraphSPARQLException;

  /**
   * Updates the knowledge graph using given SPARQL {@code query}.
   *
   * @param query which shall be executed.
   * @throws KnowledgeGraphSPARQLException if the given SPARQL query could not be executed
   * successfully.
   */
  void update(String query) throws KnowledgeGraphSPARQLException;

  /**
   * Returns the RDF4J {@link Repository} that can be used to interact with the triplestore in which
   * the knowledge graph is managed.
   *
   * @return {@link Repository}, and must not be null.
   */
  Repository getRepository();
}
