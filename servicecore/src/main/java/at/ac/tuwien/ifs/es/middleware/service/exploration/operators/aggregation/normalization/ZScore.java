package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.normalization;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.normalisation.ZScorePayload;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.AggregationOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This {@link AggregationOperator} applies a z-score normalisation to the specified fields get a
 * {@link ExplorationContext}. This operator will be registered as {@code
 * esm.aggregate.normalisation.zscore} at the {@link at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.aggregate.normalisation.zscore")
public class ZScore implements AggregationOperator<ZScorePayload> {

  private static final Logger logger = LoggerFactory.getLogger(ZScore.class);

  @Override
  public String getUID() {
    return "esm.aggregate.normalisation.zscore";
  }

  @Override
  public Class<ZScorePayload> getParameterClass() {
    return ZScorePayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, ZScorePayload payload) {
    List<JsonPointer> targets = payload.getTargets();
    if (targets != null) {
      if (!targets.isEmpty()) {
        ExplorationContext<IdentifiableResult> eContext = ((ExplorationContext<IdentifiableResult>) context);
        /* extract numbers */
        final Map<JsonPointer, List<Double>> numbersMap = new HashMap<>();
        for (JsonPointer pointer : targets) {
          numbersMap.put(pointer, new LinkedList<>());
        }
        eContext.streamOfResults().forEach(entity -> {
          for (JsonPointer pointer : targets) {
            Optional<JsonNode> valueOptional = eContext.getValues(entity.getId(), pointer);
            if (valueOptional.isPresent()) {
              JsonNode valueNode = valueOptional.get();
              if (valueNode.isNumber()) {
                numbersMap.get(pointer).add(valueNode.asDouble());
              }
            }
          }
        });
        /* compute average and standard deviation */
        final Map<JsonPointer, ZScoreInfo> zScoreInfoMap = new HashMap<>();
        for (Map.Entry<JsonPointer, List<Double>> numbersEntry : numbersMap.entrySet()) {
          double average = numbersEntry.getValue().stream().mapToDouble(number -> number)
              .average().orElse(0.0);
          double standardDeviation = 0.0;
          for (Double number : numbersEntry.getValue()) {
            standardDeviation += Math.pow(number, 2.0);
          }
          standardDeviation = Math.sqrt(standardDeviation / numbersEntry.getValue().size());
          zScoreInfoMap.put(numbersEntry.getKey(), new ZScoreInfo(average, standardDeviation));
        }
        /* compute z-score */
        eContext.streamOfResults().forEach(entity -> {
          for (JsonPointer pointer : targets) {
            Optional<JsonNode> valueOptional = eContext.getValues(entity.getId(), pointer);
            if (valueOptional.isPresent()) {
              JsonNode value = valueOptional.get();
              if (value.isNumber()) {
                ZScoreInfo zScoreInfo = zScoreInfoMap.get(pointer);
                if (zScoreInfo != null) {
                  if (Double.compare(zScoreInfo.getStandardDeviation(), 0.0) > 0) {
                    eContext
                        .putValuesData(entity.getId(), pointer, JsonNodeFactory.instance.numberNode(
                            (value.asDouble() - zScoreInfo.getAverage()) / zScoreInfo
                                .getStandardDeviation()));
                  } else {
                    eContext
                        .putValuesData(entity.getId(), pointer,
                            JsonNodeFactory.instance.numberNode(0.0));
                  }
                }
              }
            }
          }
        });
        return eContext;
      } else {
        logger.warn("Zscore for specification {} is missing a target.", payload);
        return context;
      }
    } else {
      throw new ExplorationFlowSpecificationException(
          "Given targets for the z-score operator must not be null.");
    }
  }

  private static final class ZScoreInfo {

    private Double average;
    private Double standardDeviation;

    public ZScoreInfo(Double average, Double standardDeviation) {
      this.average = average;
      this.standardDeviation = standardDeviation;
    }

    public Double getAverage() {
      return average;
    }

    public Double getStandardDeviation() {
      return standardDeviation;
    }

    @Override
    public String toString() {
      return "ZScoreInfo{" +
          "average=" + average +
          ", standardDeviation=" + standardDeviation +
          '}';
    }
  }
}
