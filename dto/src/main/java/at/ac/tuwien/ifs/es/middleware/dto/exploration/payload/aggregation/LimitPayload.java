package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public final class LimitPayload implements Serializable {

  private long number;

  @JsonCreator
  public LimitPayload(@JsonProperty(value = "number", required = true) Long number) {
    checkNotNull(number);
    checkArgument(number >= 0, "The limit number must be positive.");
    this.number = number;
  }

  public Long getNumber() {
    return number;
  }

  @Override
  public String toString() {
    return "LimitPayload{" +
        "number=" + number +
        '}';
  }
}
