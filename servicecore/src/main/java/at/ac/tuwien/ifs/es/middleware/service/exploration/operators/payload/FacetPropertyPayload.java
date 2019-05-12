package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class FacetPropertyPayload implements ExplorationFlowStepPayload {

  @JsonProperty(value = "properties", required = true)
  private List<Resource> properties;

  @JsonCreator
  public FacetPropertyPayload(
      @JsonProperty(value = "properties", required = true) List<Resource> properties) {
    this.properties = properties;
  }

  public List<Resource> getProperties() {
    return properties;
  }

  @Override
  public String toString() {
    return "FacetPropertyPayload{" +
        "properties=" + properties +
        '}';
  }
}
