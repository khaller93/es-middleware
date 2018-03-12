package at.ac.tuwien.ifs.exploratorysearch.dto.exception;

/**
 * This exception shall be thrown, if there is a problem with the format of a SPARQL result.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class QueryResultFormatException extends Exception {

  public QueryResultFormatException() {
  }

  public QueryResultFormatException(String message) {
    super(message);
  }

  public QueryResultFormatException(String message, Throwable cause) {
    super(message, cause);
  }

  public QueryResultFormatException(Throwable cause) {
    super(cause);
  }
}
