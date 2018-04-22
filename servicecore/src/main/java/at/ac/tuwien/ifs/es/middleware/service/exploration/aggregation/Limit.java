package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation.LimitPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import java.util.Iterator;
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
public class Limit implements AggregationOperator<LimitPayload> {

  @Override
  public Class<LimitPayload> getParameterClass() {
    return LimitPayload.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ExplorationContext apply(ExplorationContext context, LimitPayload payload) {
    /*int limit = payload.getNumber();
    Iterator<IdentifiableResult> iterator = context.streamOfResults().iterator();
    while (iterator.hasNext()) {
      IdentifiableResult next = iterator.next();
      if (limit > 0) {
        limit--;
      } else {
        context.removeResult(next);
      }
    }*/
    ExplorationContext<IdentifiableResult> identifiableResultsContext = (ExplorationContext<IdentifiableResult>) context;
    return identifiableResultsContext.streamOfResults().limit(payload.getNumber())
        .collect(identifiableResultsContext);
  }
}
