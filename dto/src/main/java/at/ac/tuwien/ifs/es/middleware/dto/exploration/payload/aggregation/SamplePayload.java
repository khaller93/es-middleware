package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * This is a payload for the aggregation operator {@code esm.aggregate.sample}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class SamplePayload implements Serializable {

  private Long number;

  @JsonCreator
  public SamplePayload(@JsonProperty(value = "number", required = true) Long number) {
    checkNotNull(number);
    checkArgument(number >= 0, "Given number for sample operator must be positive.");
    this.number = number;
  }

  public Long getNumber() {
    return number;
  }

  @Override
  public String toString() {
    return "SamplePayload{" +
        "number=" + number +
        '}';
  }
}
