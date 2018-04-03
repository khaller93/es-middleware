package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationResponse;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.IterableExplorationResult;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AggregationOperator} that limits the results to the given
 * number and keeps also only the values of those results. This aggregation operator can only be
 * applied to {@link IterableExplorationResult}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("LimitAggregation")
public class Limit implements AggregationOperator {

  @Override
  public ExplorationResponse apply(ExplorationResponse explorationResponse, JsonNode parameterMap) {
    ExplorationResult result = explorationResponse.getResult();
    JsonNode oldValues = explorationResponse.getValues();
    if (result instanceof IterableExplorationResult) {
      IterableExplorationResult iterableResult = (IterableExplorationResult) result;
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
    }
  }
}
