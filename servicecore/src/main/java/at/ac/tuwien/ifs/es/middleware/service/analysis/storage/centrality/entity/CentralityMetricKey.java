package at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality.entity;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.storage.ResourceEntity;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 * This is a key for caching centrality metrics.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public final class CentralityMetricKey implements Serializable {

  @Column(name = "centrality_uid")
  private String centralityUID;
  @ManyToOne
  private ResourceEntity resourceEntity;

  private CentralityMetricKey() {

  }

  private CentralityMetricKey(String centralityUID, ResourceEntity resourceEntity) {
    checkArgument(centralityUID != null && !centralityUID.isEmpty(),
        "The UID for the centrality metric should not be null or empty.");
    checkArgument(resourceEntity != null, "The resource entity must not be null.");
    this.centralityUID = centralityUID;
    this.resourceEntity = resourceEntity;
  }

  /**
   * Generates a {@link CentralityMetricKey} for the given centrality metrics and resource pair.
   *
   * @param centralityUID UID get the centrality metrics for which the key shall be created. It must
   * not be null.
   * @param resource {@link ResourceEntity} for which the key shall be created. It must not be
   * null.
   * @return the generated {@link CentralityMetricKey}.
   */
  public static CentralityMetricKey of(String centralityUID, ResourceEntity resource) {
    return new CentralityMetricKey(centralityUID, resource);
  }

  /**
   * Gets the UID get the centrality metric.
   *
   * @return UID get the centrality metric.
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
    return new Resource(resourceEntity.getResourceId());
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
        Objects.equals(resourceEntity, that.resourceEntity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(centralityUID, resourceEntity);
  }

  @Override
  public String toString() {
    return "SimilarityKey{" +
        "centralityUID='" + centralityUID + '\'' +
        ", resourceID='" + resourceEntity + '\'' +
        '}';
  }
}
