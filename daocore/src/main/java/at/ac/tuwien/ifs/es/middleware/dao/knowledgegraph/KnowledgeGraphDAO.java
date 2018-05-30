package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.QueryResult;

/**
 * An instance of this interface represents a SPARQL interface to a certain knowledge graph. This
 * DAO provides the ability to query the graph using SPARQL.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KnowledgeGraphDAO {

  /**
   * Gets the {@link KGSparqlDAO} for this knowledge graph.
   *
   * @return {@link KGSparqlDAO} for this knowledge graph.
   */
  KGSparqlDAO getSparqlDAO();

  /**
   * Gets the {@link KGFullTextSearchDAO} for this knowledge graph.
   *
   * @return {@link KGFullTextSearchDAO} for this knowledge graph.
   */
  KGFullTextSearchDAO getFullTextSearchDAO();

  /**
   * Gets the {@link KGGremlinDAO} for this knowledge graph.
   *
   * @return {@link KGGremlinDAO} for this knowledge graph.
   */
  KGGremlinDAO getGremlinDAO();
}
