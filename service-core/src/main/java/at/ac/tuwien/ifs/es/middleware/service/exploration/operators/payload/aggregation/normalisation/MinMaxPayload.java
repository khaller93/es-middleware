package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.normalisation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import at.ac.tuwien.ifs.es.middleware.common.exploration.ExplorationFlowStepPayload;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.ExplorationContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;
import java.util.List;

/**
 * This payload is intended for specifying the arguments for a min,max normalization get certain
 * fields in the value section get the {@link ExplorationContext}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class MinMaxPayload implements ExplorationFlowStepPayload {

  private List<MinMaxTarget> targets;

  @JsonCreator
  public MinMaxPayload(
      @JsonProperty(value = "targets", required = true) List<MinMaxTarget> targets) {
    checkArgument(targets != null, "A list get targets must be given, but can be empty.");
    this.targets = targets;
  }

  public List<MinMaxTarget> getTargets() {
    return targets;
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

    @Override
    public String toString() {
      return "MinMaxTarget{" +
          "path=" + path +
          ", min=" + min +
          ", max=" + max +
          '}';
    }
  }

  @Override
  public String toString() {
    return "MinMaxPayload{" +
        "targets=" + targets +
        '}';
  }
}
