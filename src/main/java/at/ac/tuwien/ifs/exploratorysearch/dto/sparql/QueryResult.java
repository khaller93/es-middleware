package at.ac.tuwien.ifs.exploratorysearch.dto.sparql;

import at.ac.tuwien.ifs.exploratorysearch.dto.exception.QueryResultFormatException;

/**
 * This class represents query results from a executed SPARQL query.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface QueryResult {

  /**
   * Transforms the query result into the given {@code format}. This method will called frequently,
   * thus it should be implemented with efficiency in mind.
   *
   * @param format MIME type of the format into which this result should be serialized.
   * @return the serialized query result in the given format.
   * @throws QueryResultFormatException if the given format is not supported by this query result.
   */
  byte[] transform(String format) throws QueryResultFormatException;

}
