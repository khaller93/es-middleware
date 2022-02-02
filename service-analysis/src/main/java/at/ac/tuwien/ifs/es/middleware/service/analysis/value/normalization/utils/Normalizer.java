package at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.NormalizationStrategy;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A normalizer that applies min,max and z-score normalization to a set of registered values. This
 * class isn't thread-safe.
 *
 * @param <ID> instances of a class that implements {@link Object#equals(Object)} and {@link
 *             Object#hashCode()}.
 * @author Kevin Haller
 * @version 1.2
 * @since 1.0
 */
public class Normalizer<ID> {

  private final Map<ID, BigDecimal> registeredMap = new LinkedHashMap<>();

  private MinMaxPair<BigDecimal> minMaxPair;
  private StatisticsPair<BigDecimal> statisticsPair;

  /**
   * Clears the cache for computed min,max and z-score values.
   */
  private void voidCache() {
    this.minMaxPair = null;
    this.statisticsPair = null;
  }

  /**
   * Registers the given {@code value} under {@code id}.
   *
   * @param id    under which the value shall be registered. It must not be {@code null}.
   * @param value that shall be registered for given {@code id}.
   * @throws IllegalArgumentException if the given {@code id} is {@code null}.
   */
  public void register(ID id, long value) {
    checkArgument(id != null, "The id must not be null.");
    registeredMap.put(id, BigDecimal.valueOf(value));
    voidCache();
  }

  /**
   * Registers the given {@code value} under {@code id}.
   *
   * @param id    under which the value shall be registered. It must not be {@code null}.
   * @param value that shall be registered for given {@code id}.
   * @throws IllegalArgumentException if the given {@code id} is {@code null}.
   */
  public void register(ID id, double value) {
    checkArgument(id != null, "The id must not be null.");
    registeredMap.put(id, BigDecimal.valueOf(value));
    voidCache();
  }

  /**
   * Registers the given {@code value} under {@code id}.
   *
   * @param id    under which the value shall be registered. It must not be {@code null}.
   * @param value that shall be registered for given {@code id}. It must not be {@code null}.
   * @throws IllegalArgumentException if the given {@code id} is {@code null}, or, if the given
   *                                  {@code value} is {@code null}.
   */
  public void register(ID id, BigDecimal value) {
    checkArgument(id != null, "The id must not be null.");
    checkArgument(value != null, "The value must not be null.");
    registeredMap.put(id, value);
    voidCache();
  }

  /**
   * Gathers all the registered {@link BigDecimal} values.
   *
   * @return all the registered {@link BigDecimal} values in a {@link Collection}.
   */
  protected Collection<V> gatherRegisteredValues() {
    return this.registeredMap.values().stream().map(v -> new V(v, 1))
        .collect(Collectors.toList());
  }

  /**
   * Gathers the minimum and maximum value of the registered collection of values.
   *
   * @return the minimum and maximum value of the registered collection of values.
   */
  protected Optional<MinMaxPair<BigDecimal>> getMinMaxValues() {
    if (minMaxPair != null) {
      return Optional.of(minMaxPair);
    } else {
      Collection<V> values = gatherRegisteredValues();
      if (values.isEmpty()) {
        return Optional.empty();
      }
      BigDecimal minValue = values.stream().reduce(V::min).get().getRawValue();
      BigDecimal maxValue = values.stream().reduce(V::max).get().getRawValue();
      MinMaxPair<BigDecimal> p = new MinMaxPair<>(minValue, maxValue);
      this.minMaxPair = p;
      return Optional.of(p);
    }
  }

  /**
   * Gathers the mean and standard deviation from all the registered values in the collection.
   *
   * @return the mean and standard deviation from all the values.
   */
  protected Optional<StatisticsPair<BigDecimal>> getZScoreStatistics() {
    if (statisticsPair != null) {
      return Optional.of(statisticsPair);
    } else {
      Collection<V> values = gatherRegisteredValues();
      if (values.isEmpty()) {
        return Optional.empty();
      }
      BigDecimal mean = values.stream().reduce(V::add).get().getRawValue()
          .divide(BigDecimal.valueOf(values.size()), MathContext.DECIMAL128);
      BigDecimal sd = values.stream().map(val -> val.subtract(new V(mean, 1)).pow(2))
          .reduce(V::add).get().getRawValue()
          .divide(BigDecimal.valueOf(values.size()), MathContext.DECIMAL128)
          .sqrt(MathContext.DECIMAL128);
      StatisticsPair<BigDecimal> statisticsPair = new StatisticsPair<>(mean, sd);
      this.statisticsPair = statisticsPair;
      return Optional.of(statisticsPair);
    }
  }

  /**
   * Gets the min-max normalization for the given {@code value}.
   *
   * @param value for which the min, max normalization shall be computed.
   */
  public Optional<BigDecimal> getMinMaxNormalizedValueFor(BigDecimal value) {
    checkArgument(value != null, "The given value must not be null.");
    Optional<MinMaxPair<BigDecimal>> optMinMaxPair = getMinMaxValues();
    if (!optMinMaxPair.isPresent()) {
      return Optional.empty();
    }
    MinMaxPair<BigDecimal> p = optMinMaxPair.get();
    boolean isSame = p.getMin().compareTo(optMinMaxPair.get().getMax()) == 0;
    boolean isNotZero = p.getMin().compareTo(BigDecimal.ZERO) != 0;
    if (isSame) {
      if (isNotZero) {
        return Optional.of(BigDecimal.ONE);
      } else {
        return Optional.of(p.getMin());
      }
    } else {
      return Optional.of(value.subtract(p.getMin()).divide(p.getMax(), MathContext.DECIMAL128));
    }
  }

  /**
   * Computes the min, max normalization.
   *
   * @return map with the min, max normalization.
   */
  private Map<ID, BigDecimal> computeMinMaxNormalization() {
    if (registeredMap.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<ID, BigDecimal> minMaxMap = new LinkedHashMap<>();
    for (Entry<ID, BigDecimal> entry : registeredMap.entrySet()) {
      Optional<BigDecimal> normalizedValue = getMinMaxNormalizedValueFor(entry.getValue());
      normalizedValue.ifPresent(bigDecimal -> minMaxMap.put(entry.getKey(), bigDecimal));
    }
    return minMaxMap;
  }

  /**
   * Gets the min-max normalization for the given {@code value}.
   *
   * @param value for which the min, max normalization shall be computed.
   */
  public Optional<BigDecimal> getZScoreNormalizedValueFor(BigDecimal value) {
    checkArgument(value != null, "The given value must not be null.");
    Optional<StatisticsPair<BigDecimal>> optStatisticsPair = getZScoreStatistics();
    if (!optStatisticsPair.isPresent()) {
      return Optional.empty();
    }
    StatisticsPair<BigDecimal> statisticsPair = optStatisticsPair.get();
    BigDecimal mean = statisticsPair.getMean();
    BigDecimal sd = statisticsPair.getStandardDeviation();
    if (sd.compareTo(BigDecimal.ZERO) != 0) {
      return Optional.of(value.subtract(mean).divide(sd, MathContext.DECIMAL128));
    } else {
      return Optional.of(BigDecimal.ZERO);
    }
  }

  /**
   * Computes the z-score normalization.
   *
   * @return map with z-score normalization.
   */
  public Map<ID, BigDecimal> computeZScoreNormalization() {
    if (registeredMap.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<ID, BigDecimal> zScoreMap = new LinkedHashMap<>();
    for (Entry<ID, BigDecimal> entry : registeredMap.entrySet()) {
      Optional<BigDecimal> normalizedValue = getZScoreNormalizedValueFor(entry.getValue());
      normalizedValue.ifPresent(bigDecimal -> zScoreMap.put(entry.getKey(), bigDecimal));
    }
    return zScoreMap;
  }

  /**
   * Performs normalization using the strategies {@link NormalizationStrategy#MinMax} and {@link
   * NormalizationStrategy#ZScore} and returns the map as result.
   *
   * @return a map with values normalized using the strategies {@link NormalizationStrategy#MinMax}
   * and {@link NormalizationStrategy#ZScore}.
   */
  public Map<ID, DecimalNormalizedAnalysisValue> normalize() {
    Map<ID, DecimalNormalizedAnalysisValue> normalizedMap = new HashMap<>();

    Map<ID, BigDecimal> minMaxMap = computeMinMaxNormalization();
    Map<ID, BigDecimal> zScoreMap = computeZScoreNormalization();
    for (ID key : registeredMap.keySet()) {
      normalizedMap.put(key, new DecimalNormalizedAnalysisValue(
          registeredMap.get(key), minMaxMap.get(key), zScoreMap.get(key)));
    }
    return normalizedMap;
  }

}
