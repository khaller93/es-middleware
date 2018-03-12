package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception;

/**
 * This exception shall be thrown, if a SPARQL query could not be executed.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLExecutionException extends Exception {

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
