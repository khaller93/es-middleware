package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation.WeightedSumPayload;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RegisterForExplorationFlow("esm.aggregate.weightedsum")
public class WeightedSum implements AggregationOperator<WeightedSumPayload> {

  private static final Logger logger = LoggerFactory.getLogger(WeightedSum.class);

  @Override
  public Class<WeightedSumPayload> getParameterClass() {
    return WeightedSumPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, WeightedSumPayload payload) {
    logger.debug("A weighted sum is computed with {}.", payload);
    JsonPointer ptr = payload.getPath();
    ExplorationContext<IdentifiableResult> eContext = context;
    if (!ptr.matches()) {
      Map<JsonPointer, Double> candidates = payload.getCandidates();
      eContext.streamOfResults().forEach(r -> {
        String id = r.getId();
        double sum = 0;
        for (Entry<JsonPointer, Double> candidate : candidates.entrySet()) {
          Optional<JsonNode> optionalValue = eContext.getValues(id, candidate.getKey());
          if (optionalValue.isPresent()) {
            JsonNode valueNode = optionalValue.get();
            if (valueNode.isValueNode()) {
              sum += candidate.getValue() * valueNode.asDouble();
            } else {
              throw new ExplorationFlowSpecificationException(
                  String.format("The given pointer '%s' does not show to a number entry.",
                      candidate.getKey()));
            }
          } else {
            throw new ExplorationFlowSpecificationException(String
                .format("There is no value associated with the given entry '%s' for '%s'.",
                    candidate.getKey(), id));
          }
        }
        context.putValuesData(r.getId(), ptr, JsonNodeFactory.instance.numberNode(sum));
      });
      return context;
    } else {
      throw new ExplorationFlowSpecificationException(
          String.format("The given path must not be empty, but was '%s'.", ptr));
    }
  }
}
