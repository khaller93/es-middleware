package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;
import java.io.Serializable;
import java.util.List;

/**
 * This class is a POJO for the parameters expected by an oder by operator. The order strategy is
 * per default {@link ORDER_STRATEGY#DESC} and can be changed with the argument {@code strategy}.
 * The argument {@code path} specifies the value that shall be used for ordering.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class OrderByPayload implements Serializable {

  public enum ORDER_STRATEGY {ASC, DESC}

  private ORDER_STRATEGY strategy = ORDER_STRATEGY.DESC;

  private JsonPointer path;

  public ORDER_STRATEGY getStrategy() {
    return strategy;
  }

  public void setStrategy(ORDER_STRATEGY strategy) {
    this.strategy = strategy;
  }

  public JsonPointer getPath() {
    return path;
  }

  @JsonProperty(value = "path", required = true)
  public void setPath(JsonPointer path) {
    this.path = path;
  }

  @Override
  public String toString() {
    return "OrderByParameterPayload{" +
        "strategy=" + strategy +
        ", path='" + path + '\'' +
        '}';
  }
}
