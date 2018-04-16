package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation.OrderByPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
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

  @Override
  public Class<OrderByPayload> getParameterClass() {
    return OrderByPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, OrderByPayload payload) {
    //Todo: implement.
    return null;
  }

}
