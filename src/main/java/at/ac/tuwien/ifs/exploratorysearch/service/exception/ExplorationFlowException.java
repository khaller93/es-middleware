package at.ac.tuwien.ifs.exploratorysearch.service.exception;

/**
 * This exception shall be thrown, if the exploration flow fails due to internal reasons.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ExplorationFlowException extends RuntimeException {

  public ExplorationFlowException() {
  }

  public ExplorationFlowException(String message) {
    super(message);
  }

  public ExplorationFlowException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExplorationFlowException(Throwable cause) {
    super(cause);
  }

}
