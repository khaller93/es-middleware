package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public final class OffsetPayload implements Serializable {

  private int number;

  @JsonCreator
  public OffsetPayload(@JsonProperty(value = "number", required = true) int number) {
    if (number < 0) {
      throw new IllegalArgumentException("The limit number must be positive.");
    }
    this.number = number;
  }

  public int getNumber() {
    return number;
  }

  @Override
  public String toString() {
    return "OffsetPayload{" +
        "number=" + number +
        '}';
  }
}
