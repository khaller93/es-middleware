package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.service.exploration.payload.aggregation.OrderByPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.payload.aggregation.OrderByPayload.ORDER_STRATEGY;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowServiceException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class OrderBy implements AggregationOperator<OrderByPayload> {

  private static final Logger logger = LoggerFactory.getLogger(OrderBy.class);

  @Override
  public Class<OrderByPayload> getParameterClass() {
    return OrderByPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, OrderByPayload payload) {
    logger.debug("The context {} is requested to be ordered by {}.", context, payload);
    ExplorationContext<IdentifiableResult> identifiableResults = (ExplorationContext<IdentifiableResult>) context;
    final Map<String, Double> valuesMap = new HashMap<>();
    for (IdentifiableResult identifiableResult : identifiableResults) {
      String id = identifiableResult.getId();
      Optional<JsonNode> optionalValueNode = identifiableResults.getValues(id, payload.getPath());
      if (optionalValueNode.isPresent()) {
        JsonNode valueNode = optionalValueNode.get();
        if (valueNode.isNumber()) {
          valuesMap.put(id, valueNode.asDouble());
        }
      } else {
        throw new ExplorationFlowServiceException(
            String.format("There is no value associated with '%s'.", payload.getPath()));
      }
    }
    final int strategy = payload.getStrategy() == ORDER_STRATEGY.ASC ? 1 : -1;
    return identifiableResults.streamOfResults().sorted(new Comparator<IdentifiableResult>() {
      @Override
      public int compare(IdentifiableResult t1, IdentifiableResult t2) {
        Double valueT1 = valuesMap.get(t1.getId());
        Double ValueT2 = valuesMap.get(t2.getId());
        if(valueT1 == null && ValueT2 == null){
          return 0;
        } else if (valueT1 == null){
          return -strategy;
        } else if (ValueT2 == null) {
          return strategy;
        }
        return strategy * Double.compare(valueT1, ValueT2);
      }
    }).collect(identifiableResults);
  }

}
