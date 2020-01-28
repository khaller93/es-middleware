package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Identifiable;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ResultCollectionContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.LimitPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.RegisterForExplorationFlow;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation get {@link AggregationOperator} that limits the results to the given
 * number and keeps also only the values get those results. This operator will be registered as
 * {@code esm.aggregate.offset} at the {@link at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow(Limit.OID)
public class Limit implements AggregationOperator<ResultCollectionContext, ResultCollectionContext, LimitPayload> {

  public static final String OID = "esm.aggregate.limit";

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
  public Class<LimitPayload> getPayloadClass() {
    return LimitPayload.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ResultCollectionContext apply(ResultCollectionContext context, LimitPayload payload) {
    ResultCollectionContext<Identifiable> identifiableResultsContext = (ResultCollectionContext<Identifiable>) context;
    return identifiableResultsContext.streamOfResults().limit(payload.getNumber())
        .collect(identifiableResultsContext.collector());
  }
}
