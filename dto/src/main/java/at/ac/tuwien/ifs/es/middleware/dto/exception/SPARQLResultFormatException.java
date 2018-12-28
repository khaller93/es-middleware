package at.ac.tuwien.ifs.es.middleware.dto.exception;

/**
 * This exception shall be thrown, if there is a problem with the format of a SPARQL result.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLResultFormatException extends Exception {

  public SPARQLResultFormatException() {
  }

  public SPARQLResultFormatException(String message) {
    super(message);
  }

  public SPARQLResultFormatException(String message, Throwable cause) {
    super(message, cause);
  }

  public SPARQLResultFormatException(Throwable cause) {
    super(cause);
  }
}
