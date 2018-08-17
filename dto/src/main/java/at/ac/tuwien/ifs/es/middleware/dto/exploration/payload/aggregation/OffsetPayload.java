package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public final class OffsetPayload implements Serializable {

  private Long number;

  @JsonCreator
  public OffsetPayload(@JsonProperty(value = "number", required = true) Long number) {
    checkNotNull(number);
    checkArgument(number >= 0);
    this.number = number;
  }

  public long getNumber() {
    return number;
  }

  @Override
  public String toString() {
    return "OffsetPayload{" +
        "number=" + number +
        '}';
  }
}
