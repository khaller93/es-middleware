package at.ac.tuwien.ifs.es.middleware.sparql.result;


import org.apache.commons.rdf.api.Graph;

/**
 * This interface represent a {@link QueryResult} that maintains the response of a CONSTRUCT or
 * DESCRIBE SPARQL query.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface GraphQueryResult extends QueryResult {

  /**
   * Returns the response of a graph query (Construct, Describe) in form of a {@link Graph}.
   *
   * @return the response of a graph query (Construct, Describe) in form of a {@link Graph}.
   */
  Graph value();

}
