package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

/**
 * This is the parameter payload for the 'multiple resource' operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class MultipleResourcesPayload implements Serializable {

  private List<Resource> resources;

  @JsonCreator
  public MultipleResourcesPayload(
      @JsonProperty(value = "resources", required = true) List<Resource> resources) {
    checkNotNull(resources);
    checkArgument(!resources.isEmpty());
    this.resources = resources;
  }

  public List<Resource> getResources() {
    return resources;
  }

  @Override
  public String toString() {
    return "MultipleResourcesPayload{" +
        "resources=" + resources +
        '}';
  }
}
