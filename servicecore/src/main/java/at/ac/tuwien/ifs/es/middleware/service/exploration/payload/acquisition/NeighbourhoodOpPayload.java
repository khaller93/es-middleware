package at.ac.tuwien.ifs.es.middleware.service.exploration.payload.acquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * This class is a POJO for the parameters expected by the neighbourhood operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class NeighbourhoodOpPayload implements Serializable {

  private List<Resource> includedProperties;
  private List<Resource> excludedProperties;

  @JsonCreator
  public NeighbourhoodOpPayload(
      @JsonProperty("includedProperties") List<Resource> includedProperties,
      @JsonProperty("excludedProperties") List<Resource> excludedProperties) {
    this.includedProperties =
        includedProperties != null ? includedProperties : Collections.emptyList();
    this.excludedProperties =
        excludedProperties != null ? excludedProperties : Collections.emptyList();
  }

  public List<Resource> getIncludedProperties() {
    return includedProperties;
  }

  public List<Resource> getExcludedProperties() {
    return excludedProperties;
  }

  @Override
  public String toString() {
    return "NeighbourhoodOpPayload{" +
        "includedProperties=" + includedProperties +
        ", excludedProperties=" + excludedProperties +
        '}';
  }
}
