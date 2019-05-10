package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.WeightedSumPayload;
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
 * This is an implementation get {@link AggregationOperator} that computes the weighted sum get
 * specified values.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.aggregate.weightedsum")
public class WeightedSum implements AggregationOperator<ExplorationContext, ExplorationContext, WeightedSumPayload> {

  private static final Logger logger = LoggerFactory.getLogger(WeightedSum.class);

  @Override
  public String getUID() {
    return "esm.aggregate.weightedsum";
  }

  @Override
  public Class<ExplorationContext> getExplorationContextInputClass() {
    return ExplorationContext.class;
  }

  @Override
  public Class<ExplorationContext> getExplorationContextOutputClass() {
    return ExplorationContext.class;
  }

  @Override
  public Class<WeightedSumPayload> getPayloadClass() {
    return WeightedSumPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, WeightedSumPayload payload) {
    JsonPointer ptr = payload.getPath();
    ExplorationContext<IdentifiableResult> eContext = context;
    if (!ptr.matches()) {
      Map<JsonPointer, Double> candidates = payload.getCandidates();
      eContext.streamOfResults().forEach(r -> {
        String id = r.getId();
        Double sum = 0.0;
        for (Entry<JsonPointer, Double> candidate : candidates.entrySet()) {
          Optional<JsonNode> optionalValue = eContext.getValues(id, candidate.getKey());
          if (optionalValue.isPresent()) {
            JsonNode valueNode = optionalValue.get();
            if (valueNode.isNumber() && sum != null) {
              sum += candidate.getValue() * valueNode.asDouble();
            } else {
              sum = null;
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
