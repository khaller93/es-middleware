package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation.normalisation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;
import java.io.Serializable;
import java.util.List;

/**
 * This payload is intended for specifying the arguments for a min,max normalisation of certain
 * fields.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class MinMaxPayload implements Serializable {

  private List<MinMaxTarget> targets;

  public List<MinMaxTarget> getTargets() {
    return targets;
  }

  public void setTargets(
      List<MinMaxTarget> targets) {
    this.targets = targets;
  }

  public static final class MinMaxTarget {

    private JsonPointer path;
    private Double min;
    private Double max;

    @JsonProperty(value = "path", required = true)
    public JsonPointer getPath() {
      return path;
    }

    public void setPath(JsonPointer path) {
      this.path = path;
    }

    public Double getMin() {
      return this.min == null ? 0 : this.min;
    }

    public void setMin(Double min) {
      this.min = min;
    }

    public Double getMax() {
      return this.max == null ? 1 : this.max;
    }

    public void setMax(Double max) {
      this.max = max;
    }
  }
}
