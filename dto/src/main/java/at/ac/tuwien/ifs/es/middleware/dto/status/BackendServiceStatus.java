package at.ac.tuwien.ifs.es.middleware.dto.status;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This DTO represents the status of a certain backend service.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class BackendServiceStatus {

  public enum STATUS {INITIATING, READY, UPDATING, FAILED}

  @JsonProperty("status")
  private STATUS status;
  @JsonProperty("error")
  @JsonInclude(Include.NON_NULL)
  private String errorMessage;

  @JsonCreator
  private BackendServiceStatus(@JsonProperty("status") STATUS status,
      @JsonProperty("error") String errorMessage) {
    this.status = status;
    this.errorMessage = errorMessage;
  }

  public static BackendServiceStatus initiating() {
    return new BackendServiceStatus(STATUS.INITIATING, null);
  }

  public static BackendServiceStatus ready() {
    return new BackendServiceStatus(STATUS.READY, null);
  }

  public static BackendServiceStatus updating() {
    return new BackendServiceStatus(STATUS.UPDATING, null);
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
