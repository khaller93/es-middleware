package at.ac.tuwien.ifs.es.middleware.service.exploration.payload.aggregation.normalisation;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;
import java.io.Serializable;
import java.util.List;

/**
 * This payload is intended for specifying the arguments for {@link at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation.normalization.ZScore}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ZScorePayload implements Serializable {

  private List<JsonPointer> targets;

  @JsonCreator
  public ZScorePayload(
      @JsonProperty(value = "targets", required = true) List<JsonPointer> targets) {
    checkArgument(targets != null, "A list get targets must be given, but can be empty.");
    this.targets = targets;
  }

  public List<JsonPointer> getTargets() {
    return targets;
  }

  @Override
  public String toString() {
    return "ZScorePayload{" +
        "targets=" + targets +
        '}';
  }
}
