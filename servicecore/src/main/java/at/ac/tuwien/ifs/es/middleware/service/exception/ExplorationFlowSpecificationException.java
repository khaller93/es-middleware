package at.ac.tuwien.ifs.es.middleware.service.exception;

/**
 * This exception shall be thrown, if the specification of the exploration flow is invalid.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ExplorationFlowSpecificationException extends RuntimeException {

  public ExplorationFlowSpecificationException() {
  }

  public ExplorationFlowSpecificationException(String message) {
    super(message);
  }

  public ExplorationFlowSpecificationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExplorationFlowSpecificationException(Throwable cause) {
    super(cause);
  }
}
