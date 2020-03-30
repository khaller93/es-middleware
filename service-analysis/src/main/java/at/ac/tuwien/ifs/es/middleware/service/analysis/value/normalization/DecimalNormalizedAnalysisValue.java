package at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization;

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
public class DoubleNormalizedAnalysisValue implements NormalizedAnalysisValue<Double> {

  private Double originalValue;
  private Map<String, BigDecimal> normValues = new HashMap<>();

  DoubleNormalizedAnalysisValue() {

  }

  DoubleNormalizedAnalysisValue(Double originalValue, Map<String, BigDecimal> normValues) {
    this.originalValue = originalValue;
    this.normValues = normValues;
  }

  public DoubleNormalizedAnalysisValue(Double originalValue) {
    this(originalValue, null, null);
  }

  public DoubleNormalizedAnalysisValue(Double originalValue, BigDecimal minMaxValue,
      BigDecimal zScoreValue) {
    this.originalValue = originalValue;
    if (minMaxValue != null) {
      normValues.put(NormalizationStrategy.MinMax.name(), minMaxValue);
    }
    if (zScoreValue != null) {
      normValues.put(NormalizationStrategy.ZScore.name(), zScoreValue);
    }
  }

  @Override
  public List<NormalizationStrategy> strategies() {
    List<NormalizationStrategy> values = new LinkedList<>();
    if (normValues.containsKey(NormalizationStrategy.MinMax.name())) {
      values.add(NormalizationStrategy.MinMax);
    }
    if (normValues.containsKey(NormalizationStrategy.ZScore.name())) {
      values.add(NormalizationStrategy.ZScore);
    }
    return values;
  }

  @Override
  public Double getValue() {
    return originalValue;
  }

  @Override
  public Optional<BigDecimal> getValueOfStrategy(NormalizationStrategy strategy) {
    if (strategies().contains(strategy)) {
      return Optional.of(normValues.get(strategy.name()));
    }
    return Optional.empty();
  }

}
