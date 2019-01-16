package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.entity;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

/**
 * This is a key for caching centrality metrics.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public final class CentralityMetricKey implements Serializable {

  private String centralityUID;
  private String resourceID;

  private CentralityMetricKey() {

  }

  private CentralityMetricKey(String centralityUID, Resource resource) {
    checkArgument(centralityUID != null && !centralityUID.isEmpty(),
        "The UID for the centrality metric should not be null or empty.");
    checkArgument(resource != null, "The key resource must not be null.");
    this.centralityUID = centralityUID;
    this.resourceID = resource.getId();
  }

  private CentralityMetricKey(String centralityUID, String resourceID) {
    checkArgument(centralityUID != null && !centralityUID.isEmpty(),
        "The UID for the centrality metric should not be null or empty.");
    checkArgument(resourceID != null && !resourceID.isEmpty(),
        "The key resource must not be specified.");
    this.centralityUID = centralityUID;
    this.resourceID = resourceID;
  }

  /**
   * Generates a {@link CentralityMetricKey} for the given centrality metrics and resource pair.
   *
   * @param centralityUID UID of the centrality metrics for which the key shall be created. It must
   * not be null.
   * @param resource {@link Resource} for which the key shall be created. It must not be null.
   * @return the generated {@link CentralityMetricKey}.
   */
  public static CentralityMetricKey of(String centralityUID, Resource resource) {
    return new CentralityMetricKey(centralityUID, resource);
  }

  /**
   * Generates a {@link CentralityMetricKey} for the given centrality metrics and resource pair.
   *
   * @param centralityUID UID of the centrality metrics for which the key shall be created. It must
   * not be null.
   * @param resourceID id of the {@link Resource} for which the key shall be created. It must not be
   * null.
   * @return the generated {@link CentralityMetricKey}.
   */
  public static CentralityMetricKey of(String centralityUID, String resourceID) {
    return new CentralityMetricKey(centralityUID, resourceID);
  }

  /**
   * Gets the UID of the centrality metric.
   *
   * @return UID of the centrality metric.
   */
  public String getCentralityUID() {
    return centralityUID;
  }

  /**
   * Gets the {@link Resource} for this key.
   *
   * @return the {@link Resource} for this key.
   */
  public Resource getResource() {
    return new Resource(resourceID);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CentralityMetricKey that = (CentralityMetricKey) o;
    return Objects.equals(centralityUID, that.centralityUID) &&
        Objects.equals(resourceID, that.resourceID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(centralityUID, resourceID);
  }

  @Override
  public String toString() {
    return "SimilarityKey{" +
        "centralityUID='" + centralityUID + '\'' +
        ", resourceID='" + resourceID + '\'' +
        '}';
  }
}
