package at.ac.tuwien.ifs.es.middleware.dto.exception;

/**
 * This exception shall be thrown, if a SPARQL query could not be executed due to some internal
 * reason like lost connection to the triplestore.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLExecutionException extends KnowledgeGraphSPARQLException {

  public SPARQLExecutionException() {
  }

  public SPARQLExecutionException(String message) {
    super(message);
  }

  public SPARQLExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public SPARQLExecutionException(Throwable cause) {
    super(cause);
  }
}
