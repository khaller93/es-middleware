package at.ac.tuwien.ifs.es.middleware.dto.status;

/**
 * This DTO represents the status of a certain backend service.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class BackendServiceStatus {

  public enum STATUS {OK, FAILED}

  private STATUS status;
  private String errorMessage;

  private BackendServiceStatus(STATUS status, String errorMessage) {
    this.status = status;
    this.errorMessage = errorMessage;
  }

  public static BackendServiceStatus ok() {
    return new BackendServiceStatus(STATUS.OK, null);
  }

  public static BackendServiceStatus failed(String errorMessage) {
    return new BackendServiceStatus(STATUS.FAILED, errorMessage);
  }

  public STATUS getStatus() {
    return status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public String toString() {
    return "BackendServiceStatus{" +
        "status=" + status +
        ", errorMessage='" + errorMessage + '\'' +
        '}';
  }
}
