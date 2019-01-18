package at.ac.tuwien.ifs.es.middleware.service.analysis.storage;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "resource_hashmap")
public class ResourceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;
  @Column(name = "resource_id", length = 4096, unique = true)
  private String resourceId;

  private ResourceEntity() {

  }

  public ResourceEntity(String resourceId) {
    checkArgument(resourceId != null && !resourceId.isEmpty(),
        "The resource id must be specified.");
    this.resourceId = resourceId;
  }

  public Long getId() {
    return id;
  }

  public String getResourceId() {
    return resourceId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceEntity that = (ResourceEntity) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "ResourceEntity{" +
        "id=" + id +
        ", resourceId='" + resourceId + '\'' +
        '}';
  }
}
