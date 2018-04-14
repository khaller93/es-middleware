package at.ac.tuwien.ifs.es.middleware.service.exception;

/**
 * This exception shall be thrown, if the exploration flow fails due to internal reasons.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ExplorationFlowServiceException extends RuntimeException {

  public ExplorationFlowServiceException() {
  }

  public ExplorationFlowServiceException(String message) {
    super(message);
  }

  public ExplorationFlowServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExplorationFlowServiceException(Throwable cause) {
    super(cause);
  }

}
