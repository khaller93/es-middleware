package at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils;


import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A normalizer that applies min,max and z-score normalization to a set of registered values.
 *
 * @param <ID> instances of a class that implements {@link Object#equals(Object)} and {@link
 * Object#hashCode()}.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class Normalizer<ID> {

  private final Map<ID, BigDecimal> registeredMap = new HashMap<>();

  /**
   * Registers the given {@code value} under {@code id}.
   *
   * @param id under which the value shall be registered.
   * @param value that shall be registered for given {@code id}.
   */
  public void register(ID id, long value) {
    checkArgument(id != null, "The id must not be null.");
    registeredMap.put(id, BigDecimal.valueOf(value));
  }

  /**
   * Registers the given {@code value} under {@code id}.
   *
   * @param id under which the value shall be registered.
   * @param value that shall be registered for given {@code id}.
   */
  public void register(ID id, double value) {
    checkArgument(id != null, "The id must not be null.");
    registeredMap.put(id, BigDecimal.valueOf(value));
  }

  /**
   * Registers the given {@code value} under {@code id}.
   *
   * @param id under which the value shall be registered.
   * @param value that shall be registered for given {@code id}.
   */
  public void register(ID id, BigDecimal value) {
    checkArgument(id != null, "The id must not be null.");
    registeredMap.put(id, value);
  }

  /**
   * Computes the min, max normalization.
   *
   * @return map with the min, max normalization.
   */
  private Map<ID, BigDecimal> minMax() {
    if (registeredMap.isEmpty()) {
      return new HashMap<>();
    }
    Map<ID, BigDecimal> minMaxMap = new HashMap<>();
    BigDecimal min = registeredMap.values().stream().min(Comparator.naturalOrder()).get();
    BigDecimal max = registeredMap.values().stream().max(Comparator.naturalOrder()).get();
    boolean zero = min.compareTo(max) == 0 && min.compareTo(BigDecimal.ZERO) == 0;
    boolean isMaxZero = !zero && max.compareTo(BigDecimal.ZERO) == 0;
    for (ID key : registeredMap.keySet()) {
      if (zero) {
        minMaxMap.put(key, min);
      } else if (isMaxZero) {
        minMaxMap.put(key, BigDecimal.ONE
            .subtract(max.subtract(registeredMap.get(key)).divide(min, MathContext.DECIMAL64)));
      } else {
        minMaxMap.put(key, registeredMap.get(key).subtract(min).divide(max, MathContext.DECIMAL64));
      }
    }
    return minMaxMap;
  }

  /**
   * Computes the z-score normalization.
   *
   * @return map with z-score normalization.
   */
  private Map<ID, BigDecimal> zScore() {
    if (registeredMap.isEmpty()) {
      return new HashMap<>();
    }
    Map<ID, BigDecimal> zScoreMap = new HashMap<>();
    BigDecimal mean = registeredMap.values().stream().reduce(BigDecimal::add).get()
        .divide(BigDecimal.valueOf(registeredMap.size()), MathContext.DECIMAL64);
    BigDecimal sd = registeredMap.values().stream().map(val -> val.subtract(mean).pow(2))
        .reduce(BigDecimal::add).get()
        .divide(BigDecimal.valueOf(registeredMap.size()), MathContext.DECIMAL64)
        .sqrt(MathContext.DECIMAL128);
    for (ID key : registeredMap.keySet()) {
      zScoreMap.put(key, registeredMap.get(key).subtract(mean).divide(sd, MathContext.DECIMAL64));
    }
    return zScoreMap;
  }

  /**
   * Performs normalization and returns the map as result.
   */
  public Map<ID, DecimalNormalizedAnalysisValue> normalize() {
    Map<ID, DecimalNormalizedAnalysisValue> normalizedMap = new HashMap<>();

    Map<ID, BigDecimal> minMaxMap = minMax();
    Map<ID, BigDecimal> zScoreMap = zScore();
    for (ID key : registeredMap.keySet()) {
      normalizedMap.put(key, new DecimalNormalizedAnalysisValue(
          registeredMap.get(key), minMaxMap.get(key), zScoreMap.get(key)));
    }
    return normalizedMap;
  }

}
