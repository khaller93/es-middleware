package at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.exception;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.QueryResult;

/**
 * This exception shall be thrown, if the serialization of a {@link QueryResult}
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
