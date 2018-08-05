package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import java.util.Objects;

/**
 * This is a key for caching similarity metrics.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class SimilarityKey {

  private String similarityUID;
  private ResourcePair resourcePair;

  private SimilarityKey(String similarityUID,
      ResourcePair resourcePair) {
    this.similarityUID = similarityUID;
    this.resourcePair = resourcePair;
  }

  public static SimilarityKey of(String similarityUID, ResourcePair resourcePair) {
    return new SimilarityKey(similarityUID, resourcePair);
  }

  public String getSimilarityUID() {
    return similarityUID;
  }

  public ResourcePair getResourcePair() {
    return resourcePair;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SimilarityKey that = (SimilarityKey) o;
    return Objects.equals(similarityUID, that.similarityUID) &&
        Objects.equals(resourcePair, that.resourcePair);
  }

  @Override
  public int hashCode() {
    return Objects.hash(similarityUID, resourcePair);
  }

  @Override
  public String toString() {
    return "SimilarityKey{" +
        "similarityUID='" + similarityUID + '\'' +
        ", resourcePair=" + resourcePair +
        '}';
  }
}
