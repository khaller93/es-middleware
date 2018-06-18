package at.ac.tuwien.ifs.es.middleware.dto.status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a failing {@link KGDAOStatus}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGDAOFailedStatus extends KGDAOStatus {

  private String errorMessage;
  private Exception exception;

  public KGDAOFailedStatus(String message, Exception exception) {
    super(CODE.FAILED);
    this.errorMessage = message;
    this.exception = exception;
  }

  @JsonProperty("error")
  @JsonInclude(Include.NON_NULL)
  public String getErrorMessage() {
    return errorMessage;
  }

  public Exception getException() {
    return exception;
  }

  @Override
  public String toString() {
    return "KGDAOFailedStatus{" +
        "code='" + getCode() + '\'' +
        "errorMessage='" + errorMessage + '\'' +
        '}';
  }
}
