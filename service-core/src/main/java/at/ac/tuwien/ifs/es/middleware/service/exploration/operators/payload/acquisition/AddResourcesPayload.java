package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.common.exploration.ExplorationFlowStepPayload;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/**
 * This payload specifies parameters for the {@link AddResourcesPayload}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class AddResourcesPayload implements ExplorationFlowStepPayload {

  private List<Resource> resources;
  private Boolean nodup;
  private Integer index;

  public AddResourcesPayload(List<Resource> resources) {
    this(resources, null, null);
  }

  public AddResourcesPayload(List<Resource> resources, Integer index) {
    this(resources, null, index);
  }

  public AddResourcesPayload(List<Resource> resources, Boolean nodup) {
    this(resources, nodup, null);
  }

  @JsonCreator
  public AddResourcesPayload(
      @JsonProperty(value = "resources", required = true) List<Resource> resources,
      @JsonProperty(value = "nodup") Boolean nodup,
      @JsonProperty(value = "index") Integer index) {
    checkArgument(resources != null, "The resource list must not be null.");
    this.resources = resources != null ? resources : Collections.emptyList();
    this.nodup = nodup != null ? nodup : false;
    this.index = index != null ? index : -1;
  }

  public List<Resource> getResources() {
    return resources;
  }

  public Boolean getNodup() {
    return nodup;
  }

  public Integer getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return "AddResourcesPayload{" +
        "resources=" + resources +
        ", nodup=" + nodup +
        ", index=" + index +
        '}';
  }
}
