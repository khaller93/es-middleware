package at.ac.tuwien.ifs.es.middleware.dto.exception;

/**
 * This exception shall be thrown, if the execution of a SPARQL query timed out.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLTimeOutException extends KnowledgeGraphSPARQLException {

  public SPARQLTimeOutException() {
  }

  public SPARQLTimeOutException(String message) {
    super(message);
  }

  public SPARQLTimeOutException(String message, Throwable cause) {
    super(message, cause);
  }

  public SPARQLTimeOutException(Throwable cause) {
    super(cause);
  }

}
