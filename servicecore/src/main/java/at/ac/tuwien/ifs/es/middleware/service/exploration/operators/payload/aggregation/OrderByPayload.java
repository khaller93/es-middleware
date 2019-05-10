package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.ExplorationFlowStepPayload;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;
import java.io.Serializable;

/**
 * This class is a POJO for the parameters expected by an oder by operator. The order strategy is
 * per default {@link ORDER_STRATEGY#ASC} and can be changed with the argument {@code strategy}. The
 * argument {@code path} specifies the value that shall be used for ordering.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class OrderByPayload implements ExplorationFlowStepPayload {

  public enum ORDER_STRATEGY {ASC, DESC}

  private ORDER_STRATEGY strategy;

  private JsonPointer path;

  @JsonCreator
  public OrderByPayload(
      @JsonProperty(value = "path", required = true) JsonPointer path,
      @JsonProperty(value = "strategy") ORDER_STRATEGY strategy) {
    checkArgument(path != null, "The path to the value that shall be ordered must be given.");
    this.path = path;
    this.strategy = strategy == null ? ORDER_STRATEGY.ASC : strategy;
  }

  public OrderByPayload(JsonPointer path) {
    this(path, null);
  }

  public ORDER_STRATEGY getStrategy() {
    return strategy;
  }

  public JsonPointer getPath() {
    return path;
  }

  @Override
  public String toString() {
    return "OrderByParameterPayload{" +
        "strategy=" + strategy +
        ", path='" + path + '\'' +
        '}';
  }
}
