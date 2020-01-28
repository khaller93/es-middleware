package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;

/**
 * The services of this application expect to be able to access the knowledge graph in
 * three different forms. The knowledge graph must be queryable with SPARQL, and the
 * graph query language Gremlin. Moreover, a fulltext search over the knowledge graph
 * must be possible.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KnowledgeGraphDAOConfig {

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
