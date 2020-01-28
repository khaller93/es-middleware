package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.normalization;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Identifiable;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.normalisation.MinMaxPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.normalisation.MinMaxPayload.MinMaxTarget;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.AggregationOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.RegisterForExplorationFlow;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This {@link AggregationOperator} applies a min,max normalisation to the specified fields get a
 * {@link ExplorationContext}. This operator will be registered as {@code
 * esm.aggregate.normalisation.minmax} at the {@link at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow(MinMaxNormalisation.OID)
public class MinMaxNormalisation implements AggregationOperator<ExplorationContext, ExplorationContext, MinMaxPayload> {

  public static final String OID = "esm.aggregate.normalisation.minmax";

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
  public Class<MinMaxPayload> getPayloadClass() {
    return MinMaxPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, MinMaxPayload payload) {
    ExplorationContext<Identifiable> eContext = ((ExplorationContext<Identifiable>) context);
    /* prepare target map */
    Map<JsonPointer, MinMaxTarget> minMaxTargetMap = new HashMap<>();
    Set<JsonPointer> minMaxPointers = minMaxTargetMap.keySet();
    for (MinMaxTarget target : payload.getTargets()) {
      minMaxTargetMap.put(target.getPath(), target);
    }
    /* build up the values table */
    Map<JsonPointer, ActualMinMax> actualMinMaxMap = new HashMap<>();
    eContext.streamOfResults().forEach((r -> {
      for (JsonPointer ptr : minMaxPointers) {
        Optional<JsonNode> valueOptional = eContext.values().get(r.getId(), ptr);
        if (valueOptional.isPresent()) {
          JsonNode valueNode = valueOptional.get();
          if (valueNode.isNumber()) {
            final double value = valueNode.asDouble();
            actualMinMaxMap.compute(ptr, (jsonPointer, actualMinMax) -> {
              if (actualMinMax == null) {
                return new ActualMinMax(value, value);
              } else {
                if (Double.compare(value, actualMinMax.getMax()) > 0) {
                  actualMinMax.setMax(value);
                } else if (Double.compare(value, actualMinMax.getMin()) < 0) {
                  actualMinMax.setMin(value);
                }
                return actualMinMax;
              }
            });
          }
        }
      }
    }));
    /* perform normalisation */
    eContext.streamOfResults().forEach(r -> {
      String id = r.getId();
      for (JsonPointer ptr : minMaxPointers) {
        Optional<JsonNode> optionalVal = eContext.values().get(id, ptr);
        if (optionalVal.isPresent() && optionalVal.get().isNumber()) {
          ActualMinMax actualMinMax = actualMinMaxMap.get(ptr);
          double range = actualMinMax.getMax() - actualMinMax.getMin();
          if (Double.compare(range, 0.0) != 0) {
            double val = (optionalVal.get().asDouble() - actualMinMax.getMin()) / range;
            eContext.values().put(id, ptr, JsonNodeFactory.instance.numberNode(val));
          } else {
            eContext.values().put(id, ptr, JsonNodeFactory.instance.numberNode(1.0));
          }
        }
      }
    });
    return eContext;
  }

  private static final class ActualMinMax {

    private Double min;
    private Double max;

    public ActualMinMax(Double min, Double max) {
      this.min = min;
      this.max = max;
    }

    public void setMin(Double min) {
      this.min = min;
    }

    public void setMax(Double max) {
      this.max = max;
    }

    public Double getMin() {
      return min;
    }

    public Double getMax() {
      return max;
    }

    @Override
    public String toString() {
      return "ActualMinMax{" +
          "min=" + min +
          ", max=" + max +
          '}';
    }
  }
}
