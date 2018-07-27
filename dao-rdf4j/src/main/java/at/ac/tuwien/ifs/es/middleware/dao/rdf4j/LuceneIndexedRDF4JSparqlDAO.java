package at.ac.tuwien.ifs.es.middleware.dao.rdf4j;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;

/**
 * This is a marker interface for RDF4J triplestores that use Lucene for indexing of text.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface LuceneIndexedRDF4JSparqlDAO extends KGSparqlDAO {

}
