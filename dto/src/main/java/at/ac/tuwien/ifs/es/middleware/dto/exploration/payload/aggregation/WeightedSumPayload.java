package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation;

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
public final class WeightedSumPayload implements Serializable {

  private JsonPointer path;
  private Map<JsonPointer, Double> candidates;

  @JsonProperty(value = "path", required = true)
  public JsonPointer getPath() {
    return path;
  }

  public void setPath(JsonPointer path) {
    this.path = path;
  }

  @JsonProperty(value = "candidates", required = true)
  public Map<JsonPointer, Double> getCandidates() {
    return candidates;
  }

  public void setCandidates(
      Map<JsonPointer, Double> candidates) {
    this.candidates = candidates;
  }
}
