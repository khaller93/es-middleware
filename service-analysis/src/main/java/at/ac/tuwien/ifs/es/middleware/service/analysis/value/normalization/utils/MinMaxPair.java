package at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigDecimal;

/**
 * A pair of min-value and max-value of a certain datatype (e.g. {@link Long} or {@link
 * BigDecimal}).
 *
 * @author Kevin Haller
 * @version 1.2
 * @since 1.2
 */
public final class MinMaxPair<T> {

  private final T min;
  private final T max;

  public MinMaxPair(T min, T max) {
    checkNotNull(min);
    checkNotNull(max);
    this.min = min;
    this.max = max;
  }

  public T getMin() {
    return min;
  }

  public T getMax() {
    return max;
  }

  @Override
  public String toString() {
    return "MinMaxPair{" +
        "min=" + min +
        ", max=" + max +
        '}';
  }
}
