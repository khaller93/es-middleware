package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation.WeightedSumPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AggregationOperator} that computes the weighted sum of
 * specified values.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.aggregate.weighted-sum")
public class WeightedSum implements AggregationOperator<WeightedSumPayload> {

  @Override
  public Class<WeightedSumPayload> getParameterClass() {
    return null;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, WeightedSumPayload payload) {
    return null;
  }
}
