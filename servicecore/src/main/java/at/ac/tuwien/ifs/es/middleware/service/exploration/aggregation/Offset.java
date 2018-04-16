package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation.OffsetPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AggregationOperator} that skips a given number of results.
 * This operator will be registered as {@code esm.aggregate.offset} at the {@link
 * at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.aggregate.offset")
public class Offset implements AggregationOperator<OffsetPayload> {

  @Override
  public Class<OffsetPayload> getParameterClass() {
    return OffsetPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, OffsetPayload payload) {
    //TODO: Implement.
    return null;
  }

}
