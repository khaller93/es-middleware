package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation;

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

  private int number;

  @JsonCreator
  public SamplePayload(@JsonProperty(value = "number", required = true) int number) {
    this.number = number;
  }

  public int getNumber() {
    return number;
  }

  @Override
  public String toString() {
    return "SamplePayload{" +
        "number=" + number +
        '}';
  }
}
