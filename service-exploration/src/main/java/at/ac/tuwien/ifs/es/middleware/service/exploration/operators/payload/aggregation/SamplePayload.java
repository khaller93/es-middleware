package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStepPayload;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is a payload for {@link SamplePayload} that expects a non negative number. If not given, an
 * {@link IllegalArgumentException} will be thrown.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class SamplePayload implements ExplorationFlowStepPayload {

  private Long number;

  @JsonCreator
  public SamplePayload(@JsonProperty(value = "number", required = true) Long number) {
    checkArgument(number != null && number >= 0,
        "Given number for sample operator must be non negative, but was %d.", number);
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
