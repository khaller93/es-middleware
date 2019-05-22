package at.ac.tuwien.ifs.es.middleware.dto.sparql;

/**
 * This interface represent a {@link QueryResult} that maintains the response of a ASK SPARQL query.
 * This is either {@code true}, or {@code false}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AskQueryResult extends QueryResult {

  /**
   * Returns the response of an ASK query, either {@code true}, or {@code false}.
   *
   * @return response of ASK query, either {@code true}, or {@code false}.
   */
  boolean value();

}
