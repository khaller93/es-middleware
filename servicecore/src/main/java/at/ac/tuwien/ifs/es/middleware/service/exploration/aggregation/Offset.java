package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.databind.JsonNode;
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
public class Offset implements AggregationOperator {

  @Override
  public Class<Integer> getParameterClass() {
    return Integer.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext explorationContext, JsonNode parameterMap) {
    //TODO: Implement
    return null;
  }


}
