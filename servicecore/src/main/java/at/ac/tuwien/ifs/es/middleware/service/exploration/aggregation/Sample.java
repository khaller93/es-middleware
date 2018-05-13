package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation.SamplePayload;
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
 * This is an implementation of {@link AggregationOperator} that draws {@code n} random samples,
 * where {@code n} is specified in the passed {@link SamplePayload}. This operator will be
 * registered as {@code esm.aggregate.sample} at the {@link at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.aggregate.sample")
public class Sample implements AggregationOperator<SamplePayload> {

  private static final Logger logger = LoggerFactory.getLogger(Sample.class);

  @Override
  public Class<SamplePayload> getParameterClass() {
    return SamplePayload.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ExplorationContext apply(ExplorationContext context, SamplePayload payload) {
    logger.debug("Apply 'sample' operator to {} with {}.", context, payload);
    int n = payload.getNumber();
    if (n >= 0) {
      Random randomGenerator = new Random();
      ExplorationContext<IdentifiableResult> eContext = (ExplorationContext<IdentifiableResult>) context;
      List<IdentifiableResult> results = eContext.streamOfResults()
          .collect(Collectors.toCollection(ArrayList::new));
      List<IdentifiableResult> sampledResults = new LinkedList<>();
      for (int i = 0; i < n; i++) {
        sampledResults.add(results.remove(randomGenerator.nextInt(results.size())));
      }
      return sampledResults.stream().collect(eContext);
    } else {
      logger.error(
          "A negative number ({}) was passed for aggregation operator sample, but must be positive.",
          n);
      return context;
    }
  }
}
