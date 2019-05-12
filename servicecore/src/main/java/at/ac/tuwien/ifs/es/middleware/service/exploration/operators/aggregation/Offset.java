package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResultCollectionContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.OffsetPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation get {@link AggregationOperator} that skips a given number get results.
 * This operator will be registered as {@code esm.aggregate.offset} at the {@link
 * at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow(Offset.OID)
public class Offset implements AggregationOperator<ResultCollectionContext, ResultCollectionContext, OffsetPayload> {

  public static final String OID = "esm.aggregate.offset";

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
  public Class<OffsetPayload> getPayloadClass() {
    return OffsetPayload.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ResultCollectionContext apply(ResultCollectionContext context, OffsetPayload payload) {
    ResultCollectionContext<IdentifiableResult> identifiableResultsContext = (ResultCollectionContext<IdentifiableResult>) context;
    return identifiableResultsContext.streamOfResults().skip(payload.getNumber())
        .collect(identifiableResultsContext.collector());
  }

}
