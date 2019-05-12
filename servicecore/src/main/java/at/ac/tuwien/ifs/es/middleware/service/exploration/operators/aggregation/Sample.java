package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResultCollectionContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.SamplePayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation get {@link AggregationOperator} that draws {@code n} random samples,
 * where {@code n} is specified in the passed {@link SamplePayload}. This operator will be
 * registered as {@code esm.aggregate.sample} at the {@link at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow(Sample.OID)
public class Sample implements AggregationOperator<ResultCollectionContext, ResultCollectionContext, SamplePayload> {

  public static final String OID = "esm.aggregate.sample";

  private static final Logger logger = LoggerFactory.getLogger(Sample.class);

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
  public Class<SamplePayload> getPayloadClass() {
    return SamplePayload.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ResultCollectionContext apply(ResultCollectionContext context, SamplePayload payload) {
    Long n = payload.getNumber();
    if (n >= 0) {
      Random randomGenerator = new Random();
      ResultCollectionContext<IdentifiableResult> eContext = (ResultCollectionContext<IdentifiableResult>) context;
      List<IdentifiableResult> results = eContext.streamOfResults()
          .collect(Collectors.toCollection(ArrayList::new));
      List<IdentifiableResult> sampledResults = new LinkedList<>();
      for (int i = 0; i < n && !results.isEmpty(); i++) {
        sampledResults.add(results.remove(randomGenerator.nextInt(results.size())));
      }
      return sampledResults.stream().collect(eContext.collector());
    } else {
      logger.error(
          "A negative number ({}) was passed for aggregation operator sample, but must be positive.",
          n);
      return context;
    }
  }
}
