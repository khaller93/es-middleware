package at.ac.tuwien.ifs.es.middleware.controller.meta;

import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * This class is a DTO for exception occurring in {@link ExplorationFlow}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class EFExceptionDTO {

  private String message;
  private Instant timestamp;

  public EFExceptionDTO(String message) {
    this(message, Instant.now());
  }

  @JsonCreator
  private EFExceptionDTO(@JsonProperty(value = "message", required = true) String message,
      @JsonProperty(value = "timestamp", required = true) Instant timestamp) {
    this.message = message;
    this.timestamp = timestamp;
  }

  public String getMessage() {
    return message;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "EFExceptionDTO{" +
        "message='" + message + '\'' +
        ", timestamp=" + timestamp +
        '}';
  }
}
