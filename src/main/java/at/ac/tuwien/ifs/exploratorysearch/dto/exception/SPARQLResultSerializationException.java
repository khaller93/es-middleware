package at.ac.tuwien.ifs.exploratorysearch.dto.exception;

/**
 * This exception shall be thrown, if the serialization of a {@link at.ac.tuwien.ifs.exploratorysearch.dto.sparql.QueryResult}
 * fails.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLResultSerializationException extends RuntimeException {

  public SPARQLResultSerializationException() {
  }

  public SPARQLResultSerializationException(String message) {
    super(message);
  }

  public SPARQLResultSerializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public SPARQLResultSerializationException(Throwable cause) {
    super(cause);
  }
}
