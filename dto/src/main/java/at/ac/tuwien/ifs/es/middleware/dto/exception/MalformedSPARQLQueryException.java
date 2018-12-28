package at.ac.tuwien.ifs.es.middleware.dto.exception;

/**
 * This exception shall be thrown, if the client handed over a malformed query.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class MalformedSPARQLQueryException extends KnowledgeGraphSPARQLException {

  public MalformedSPARQLQueryException() {
  }

  public MalformedSPARQLQueryException(String message) {
    super(message);
  }

  public MalformedSPARQLQueryException(String message, Throwable cause) {
    super(message, cause);
  }

  public MalformedSPARQLQueryException(Throwable cause) {
    super(cause);
  }
}
