package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.entity;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

/**
 * This is a key for caching similarity metrics.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public final class SimilarityMetricKey implements Serializable {

  private String similarityUID;
  private String leftResourceID;
  private String rightResourceID;

  private SimilarityMetricKey() {

  }

  private SimilarityMetricKey(String similarityUID, ResourcePair resourcePair) {
    checkArgument(similarityUID != null && !similarityUID.isEmpty(),
        "The UID for the similarity metric should not be null or empty.");
    checkArgument(resourcePair != null, "The resource pair must not be null.");
    this.similarityUID = similarityUID;
    this.leftResourceID = resourcePair.getFirst().getId();
    this.rightResourceID = resourcePair.getSecond().getId();
  }

  /**
   * Generates a {@link SimilarityMetricKey} for the given similarity metrics and resource pair.
   *
   * @param similarityUID UID of the similarity metrics for which the key shall be created. It must
   * not be null.
   * @param resourcePair {@link ResourcePair} for which the key shall be created. It must not be
   * null.
   * @return the generated {@link SimilarityMetricKey}.
   */
  public static SimilarityMetricKey of(String similarityUID, ResourcePair resourcePair) {
    return new SimilarityMetricKey(similarityUID, resourcePair);
  }

  /**
   * Gets the UID of the similarity metric.
   *
   * @return UID of the similarity metric.
   */
  public String getSimilarityUID() {
    return similarityUID;
  }

  /**
   * Gets the {@link ResourcePair} for this key.
   *
   * @return the {@link ResourcePair} for this key.
   */
  public ResourcePair getResourcePair() {
    return ResourcePair.of(new Resource(leftResourceID), new Resource(rightResourceID));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SimilarityMetricKey that = (SimilarityMetricKey) o;
    return Objects.equals(similarityUID, that.similarityUID) &&
        Objects.equals(leftResourceID, that.leftResourceID) &&
        Objects.equals(rightResourceID, that.rightResourceID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(similarityUID, leftResourceID, rightResourceID);
  }

  @Override
  public String toString() {
    return "SimilarityKey{" +
        "similarityUID='" + similarityUID + '\'' +
        ", leftResourceID='" + leftResourceID + '\'' +
        ", rightResourceID='" + rightResourceID + '\'' +
        '}';
  }
}
