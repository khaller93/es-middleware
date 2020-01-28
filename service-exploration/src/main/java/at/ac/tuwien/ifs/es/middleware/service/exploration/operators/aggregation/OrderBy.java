package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Identifiable;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ResultCollectionContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.OrderByPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.OrderByPayload.ORDER_STRATEGY;
import at.ac.tuwien.ifs.es.middleware.service.exploration.exception.ExplorationFlowServiceExecutionException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.RegisterForExplorationFlow;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation get {@link AggregationOperator} that orders the results by the specified
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
@RegisterForExplorationFlow(OrderBy.OID)
public class OrderBy implements AggregationOperator<ResultCollectionContext, ResultCollectionContext, OrderByPayload> {

  public static final String OID = "esm.aggregate.orderby";

  @Override
  public String getUID() {
    return OID;
  }

  @Override
  public Class<ResultCollectionContext> getExplorationContextInputClass() {
    return ResultCollectionContext.class;
  }

  @Override
  public Class<ResultCollectionContext> getExplorationContextOutputClass() {
    return ResultCollectionContext.class;
  }

  @Override
  public Class<OrderByPayload> getPayloadClass() {
    return OrderByPayload.class;
  }

  @Override
  public ResultCollectionContext apply(ResultCollectionContext context, OrderByPayload payload) {
    ResultCollectionContext<Identifiable> identifiableResults = (ResultCollectionContext<Identifiable>) context;
    final Map<String, Double> valuesMap = new HashMap<>();
    for (Identifiable identifiableResult : identifiableResults) {
      String id = identifiableResult.getId();
      Optional<JsonNode> optionalValueNode = identifiableResults.values().get(id, payload.getPath());
      if (optionalValueNode.isPresent()) {
        JsonNode valueNode = optionalValueNode.get();
        if (valueNode.isNumber()) {
          valuesMap.put(id, valueNode.asDouble());
        }
      } else {
        throw new ExplorationFlowServiceExecutionException(
            String.format("There is no value associated with '%s'.", payload.getPath()));
      }
    }
    final int strategy = payload.getStrategy() == ORDER_STRATEGY.ASC ? 1 : -1;
    return identifiableResults.streamOfResults().sorted(new Comparator<Identifiable>() {
      @Override
      public int compare(Identifiable t1, Identifiable t2) {
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
    }).collect(identifiableResults.collector());
  }

}
