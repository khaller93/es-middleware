package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * A parameter payload for {@link at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.Limit}
 * that expects a non negative number. If not given, an {@link IllegalArgumentException} will be
 * thrown.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class LimitPayload implements Serializable {

  private long number;

  @JsonCreator
  public LimitPayload(@JsonProperty(value = "number", required = true) Long number) {
    checkArgument(number != null && number >= 0,
        "The limit number must be non negative, but was %d.", number);
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
