package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition.RemoveResourcesOperator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Set;

/**
 * This payload specifies parameters for the {@link RemoveResourcesOperator}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class RemoveResourcesPayload implements Serializable {

  private Set<Resource> resources;

  @JsonCreator
  public RemoveResourcesPayload(
      @JsonProperty(value = "resources", required = true) Set<Resource> resources) {
    checkArgument(resources != null, "The resource set to remove must not be null.");
    this.resources = resources;
  }

  public Set<Resource> getResources() {
    return resources;
  }

  @Override
  public String toString() {
    return "RemoveResourcesPayload{" +
        "resources=" + resources +
        '}';
  }
}
