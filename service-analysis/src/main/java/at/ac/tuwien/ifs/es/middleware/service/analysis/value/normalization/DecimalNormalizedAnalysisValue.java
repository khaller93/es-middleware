package at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue.Serializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link NormalizedAnalysisValue} for metrics with floating numbers.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonSerialize(using = Serializer.class)
public class DecimalNormalizedAnalysisValue implements NormalizedAnalysisValue<BigDecimal>,
    Serializable {

  private Map<String, BigDecimal> valueMap = new HashMap<>();

  public DecimalNormalizedAnalysisValue(BigDecimal value) {
    this(value, null, null);
  }

  public DecimalNormalizedAnalysisValue(BigDecimal originalValue, BigDecimal minMaxValue,
      BigDecimal zScoreValue) {
    checkArgument(originalValue != null, "The original value must not be null.");
    valueMap.put("value", originalValue);
    if (minMaxValue != null) {
      valueMap.put(NormalizationStrategy.MinMax.name().toLowerCase(), minMaxValue);
    }
    if (zScoreValue != null) {
      valueMap.put(NormalizationStrategy.ZScore.name().toLowerCase(), zScoreValue);
    }
  }

  @JsonIgnore
  @Override
  public List<NormalizationStrategy> strategies() {
    List<NormalizationStrategy> values = new LinkedList<>();
    if (valueMap.containsKey(NormalizationStrategy.MinMax.name().toLowerCase())) {
      values.add(NormalizationStrategy.MinMax);
    }
    if (valueMap.containsKey(NormalizationStrategy.ZScore.name().toLowerCase())) {
      values.add(NormalizationStrategy.ZScore);
    }
    return values;
  }

  @JsonIgnore
  @Override
  public BigDecimal getValue() {
    return valueMap.get("value");
  }

  @Override
  public Optional<BigDecimal> getValueOfStrategy(NormalizationStrategy strategy) {
    if (strategies().contains(strategy)) {
      return Optional.of(valueMap.get(strategy.name().toLowerCase()));
    }
    return Optional.empty();
  }

  static class Serializer extends JsonSerializer<DecimalNormalizedAnalysisValue> {

    @Override
    public void serialize(DecimalNormalizedAnalysisValue value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      gen.writeStartObject();
      for (Map.Entry<String, BigDecimal> entry : value.valueMap.entrySet()) {
        gen.writeNumberField(entry.getKey(), entry.getValue());
      }
      gen.writeEndObject();
    }
  }

}
