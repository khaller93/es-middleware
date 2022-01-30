package at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A
 *
 * @author Kevin Haller
 * @version 1.2
 * @since 1.2
 */
public final class StatisticsPair<T> {

  private final T mean;
  private final T standardDeviation;

  public StatisticsPair(T mean, T standardDeviation) {
    checkNotNull(mean);
    checkNotNull(standardDeviation);
    this.mean = mean;
    this.standardDeviation = standardDeviation;
  }

  public T getMean() {
    return mean;
  }

  public T getStandardDeviation() {
    return standardDeviation;
  }

  @Override
  public String toString() {
    return "StatisticsPair{" +
        "mean=" + mean +
        ", standardDeviation=" + standardDeviation +
        '}';
  }
}
