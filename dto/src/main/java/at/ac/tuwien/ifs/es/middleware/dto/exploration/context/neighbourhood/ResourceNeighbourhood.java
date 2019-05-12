package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.neighbourhood;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.ResourceJsonComponent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class represents the neighbourhood of a certain resource. A neighbourhood describes the
 * outgoing properties and the corresponding objects (which are resources).
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourceNeighbourhood implements IdentifiableResult {

  @JsonIgnore
  private static final AtomicLong idCounter1 = new AtomicLong(1L);
  @JsonIgnore
  private static final AtomicLong idCounter2 = new AtomicLong(1L);

  /**
   * Creates a new neighbourhood of a {@link Resource} with the given property map and assigns a new
   * id.
   *
   * @param propertyMap a map of properties and its objects.
   * @return a new empty neighbourhood for the given {@link Resource}.
   */
  public static ResourceNeighbourhood of(Map<Resource, List<Resource>> propertyMap) {
    String id = "#" + idCounter1.get() + "." + idCounter2.getAndUpdate(l -> {
      if (l == Long.MAX_VALUE) {
        idCounter1.incrementAndGet();
        return 1L;
      } else {
        return l + 1;
      }
    });
    return new ResourceNeighbourhood(id, propertyMap);
  }

  private String id;
  @JsonSerialize(keyUsing = ResourceJsonComponent.MapSerializer.class)
  @JsonDeserialize(keyUsing = ResourceJsonComponent.MapDeserializer.class)
  private Map<Resource, List<Resource>> properties;

  @JsonCreator
  public ResourceNeighbourhood(@JsonProperty(value = "id", required = true) String id,
      @JsonProperty(value = "properties") Map<Resource, List<Resource>> properties) {
    checkArgument(id != null && !id.isEmpty(),
        "An id must be specified for the neighbourhood of a resource.");
    this.id = id;
    this.properties = properties != null ? properties : Collections.emptyMap();
  }

  @Override
  public String getId() {
    return id;
  }

  public Map<Resource, List<Resource>> getProperties() {
    return properties;
  }

  @Override
  public String toString() {
    return "ResourceNeighbourhood{" +
        "id='" + id + '\'' +
        ", properties=" + properties +
        '}';
  }
}
