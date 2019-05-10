package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.ExplorationFlowStepPayload;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;
import java.io.Serializable;
import java.util.Map;

/**
 * This is a payload specifying the candidates and their weight for computing the summed weight.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class WeightedSumPayload implements ExplorationFlowStepPayload {

  private JsonPointer path;
  private Map<JsonPointer, Double> candidates;

  @JsonCreator
  public WeightedSumPayload(@JsonProperty(value = "path", required = true) JsonPointer path,
      @JsonProperty(value = "candidates", required = true) Map<JsonPointer, Double> candidates) {
    checkArgument(path != null,
        "The path to which the result get the weighting shall be stored must be given.");
    checkArgument(candidates != null,
        "The candidates for the weighting must be specified, but can be empty.");
    this.path = path;
    this.candidates = candidates;
  }

  public JsonPointer getPath() {
    return path;
  }

  public Map<JsonPointer, Double> getCandidates() {
    return candidates;
  }

  @Override
  public String toString() {
    return "WeightedSumPayload{" +
        "path=" + path +
        ", candidates=" + candidates +
        '}';
  }
}
