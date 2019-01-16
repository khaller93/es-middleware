package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.entity;

import static com.google.common.base.Preconditions.checkArgument;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "centrality_metric_result")
public class CentralityMetricResult {

  @EmbeddedId
  private CentralityMetricKey key;
  private Number value;

  private CentralityMetricResult() {

  }

  public CentralityMetricResult(CentralityMetricKey key, Number value) {
    checkArgument(key != null,
        "The key must be specified for the centrality measurement.");
    this.key = key;
    this.value = value;
  }

  /**
   * Gets the {@link CentralityMetricKey} of this result.
   *
   * @return the {@link CentralityMetricKey} of this result.
   */
  public CentralityMetricKey getKey() {
    return key;
  }

  /**
   * Gets the value of the result, which is casted to the expected type.
   *
   * @param <T> type that is expected.
   * @return the value of the result, which is casted to the expected type.
   */
  public <T extends Number> T getValue() {
    return (T) value;
  }

  @Override
  public String toString() {
    return "CentralityMetricResult{" +
        "key=" + key +
        ", value=" + value +
        '}';
  }
}
