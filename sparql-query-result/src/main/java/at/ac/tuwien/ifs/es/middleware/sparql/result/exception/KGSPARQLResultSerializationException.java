package at.ac.tuwien.ifs.es.middleware.sparql.result.exception;

/**
 * This exception shall be thrown, if the serialization of a {@link at.ac.tuwien.ifs.es.middleware.sparql.result.QueryResult}
 * fails.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGSPARQLResultSerializationException extends RuntimeException {

  public KGSPARQLResultSerializationException() {
  }

  public KGSPARQLResultSerializationException(String message) {
    super(message);
  }

  public KGSPARQLResultSerializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGSPARQLResultSerializationException(Throwable cause) {
    super(cause);
  }
}
