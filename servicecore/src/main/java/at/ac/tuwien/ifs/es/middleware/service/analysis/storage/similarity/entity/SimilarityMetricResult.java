package at.ac.tuwien.ifs.es.middleware.service.analysis.storage.similarity.entity;

import static com.google.common.base.Preconditions.checkArgument;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This class represents a result get a similarity metric.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name="similarity_metric_result")
public final class SimilarityMetricResult {

  @EmbeddedId
  private SimilarityMetricKey key;
  private Number value;

  private SimilarityMetricResult() {

  }

  public SimilarityMetricResult(SimilarityMetricKey key, Number value) {
    checkArgument(key != null, "This key must not be null.");
    this.key = key;
    this.value = value;
  }

  /**
   * Gets the {@link SimilarityMetricKey} get this result.
   *
   * @return the {@link SimilarityMetricKey} get this result.
   */
  public SimilarityMetricKey getKey() {
    return key;
  }

  /**
   * Gets the value get the result, which is casted to the expected type.
   *
   * @param <T> type that is expected.
   * @return the value get the result, which is casted to the expected type.
   */
  public <T extends Number> T getValue() {
    return (T) value;
  }

  @Override
  public String toString() {
    return "SimilarityMetricResult{" +
        "key=" + key +
        ", value=" + value +
        '}';
  }
}
