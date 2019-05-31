package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.WeightedSumPayload;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.common.exploration.RegisterForExplorationFlow;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
public class WeightedSum implements
    AggregationOperator<ExplorationContext, ExplorationContext, WeightedSumPayload> {

  public static final String OID = "esm.aggregate.weightedsum";

  @Override
  public String getUID() {
    return OID;
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
    ExplorationContext<IdentifiableResult> eContext = context;
    Map<JsonPointer, Double> candidates = payload.getCandidates();
    eContext.streamOfResults().forEach(r -> {
      String id = r.getId();
      Double sum = 0.0;
      for (Entry<JsonPointer, Double> candidate : candidates.entrySet()) {
        Optional<JsonNode> optionalValue = eContext.values().get(id, candidate.getKey());
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
      context.values().put(r.getId(), payload.getPath(), JsonNodeFactory.instance.numberNode(sum));
    });
    return context;
  }
}
