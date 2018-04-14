package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.IterableExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AggregationOperator} that limits the results to the given
 * number and keeps also only the values of those results. This operator will be registered as
 * {@code esm.aggregate.offset} at the {@link at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.aggregate.limit")
public class Limit implements AggregationOperator {

  @Override
  public Class<?> getParameterClass() {
    return Integer.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext explorationContext, JsonNode parameterMap) {
    /*ExplorationResponse result = explorationResponse.getResult();
    JsonNode oldValues = explorationResponse.getValues();
    if (result instanceof IterableExplorationResponse) {
      IterableExplorationResponse iterableResult = (IterableExplorationResponse) result;
      if (parameterMap.has("number")) {
        long limitNr = parameterMap.get("number").asLong();
        if (limitNr > 0) {
          ObjectNode newValues = JsonNodeFactory.instance.objectNode();
          //TODO: Implement
          return null;
        } else {
          throw new ExplorationFlowSpecificationException(String
              .format("The specified number for 'Limit' must be greater than zero, but was %d.",
                  limitNr));
        }
      } else {
        throw new ExplorationFlowSpecificationException(
            "'Limit' aggregation requires a 'number' to be specified in the parameter map.");
      }
    } else {
      throw new ExplorationFlowSpecificationException(
          "'Limit' aggregation can only be applied to iterable results.");
    }*/
    return null;
  }
}
