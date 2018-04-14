package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AggregationOperator} that orders the results by the specified
 * value. The value is specified as a path in the value payload starting from the individual result.
 * This operator will be registered as {@code esm.aggregate.orderby} at the {@link
 * at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.aggregate.orderby")
public class OrderBy implements AggregationOperator {

  @Override
  public Class<OrderByParameterPayload> getParameterClass() {
    return OrderByParameterPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext explorationContext, JsonNode parameterMap) {
    //TODO: Implement
    return null;
  }

  /**
   * This class is a POJO for the parameters expected by this {@link OrderBy}. The order strategy is
   * per default {@link ORDER_STRATEGY#DESC} and can be changed with the argument {@code strategy}.
   * The argument {@code path} specifies the value that shall be used for ordering.
   */
  private static final class OrderByParameterPayload {

    private enum ORDER_STRATEGY {ASC, DESC}

    private ORDER_STRATEGY strategy = ORDER_STRATEGY.DESC;
    @NotNull
    private List<String> path;

    public ORDER_STRATEGY getStrategy() {
      return strategy;
    }

    public void setStrategy(ORDER_STRATEGY strategy) {
      this.strategy = strategy;
    }

    public List<String> getPath() {
      return path;
    }

    public void setPath(List<String> path) {
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

}
