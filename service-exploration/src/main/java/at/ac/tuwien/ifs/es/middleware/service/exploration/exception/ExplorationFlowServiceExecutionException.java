package at.ac.tuwien.ifs.es.middleware.service.exploration.exception;

/**
 * This exception shall be thrown, if the exploration flow fails due to internal reasons.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ExplorationFlowServiceExecutionException extends RuntimeException {

  public ExplorationFlowServiceExecutionException() {
  }

  public ExplorationFlowServiceExecutionException(String message) {
    super(message);
  }

  public ExplorationFlowServiceExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExplorationFlowServiceExecutionException(Throwable cause) {
    super(cause);
  }

}
