package at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.exception;

/**
 * This exception shall be thrown, if there is a problem with the format of a SPARQL result.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGSPARQLResultFormatException extends RuntimeException {

  public KGSPARQLResultFormatException() {
  }

  public KGSPARQLResultFormatException(String message) {
    super(message);
  }

  public KGSPARQLResultFormatException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGSPARQLResultFormatException(Throwable cause) {
    super(cause);
  }
}
