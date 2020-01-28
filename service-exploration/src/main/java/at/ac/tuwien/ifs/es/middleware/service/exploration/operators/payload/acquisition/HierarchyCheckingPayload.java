package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStepPayload;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class HierarchyCheckingPayload implements ExplorationFlowStepPayload {

  private Set<Resource> includeClasses;
  private Set<Resource> excludeClasses;

  private Set<Resource> topDownProperties;
  private Set<Resource> bottomUpProperties;

  @JsonCreator
  public HierarchyCheckingPayload(@JsonProperty(value = "includeClasses") Set<Resource> includeClasses,
      @JsonProperty(value = "excludeClasses") Set<Resource> excludeClasses,
      @JsonProperty(value = "topDownProperties") Set<Resource> topDownProperties,
      @JsonProperty(value = "bottomUpProperties") Set<Resource> bottomUpProperties) {
    this.includeClasses = includeClasses != null ? includeClasses : new HashSet<>();
    this.excludeClasses = excludeClasses != null ? excludeClasses : new HashSet<>();
    this.topDownProperties = topDownProperties != null ? topDownProperties : new HashSet<>();
    this.bottomUpProperties = bottomUpProperties != null ? bottomUpProperties : new HashSet<>();
  }

  public Set<Resource> getIncludeClasses() {
    return includeClasses;
  }

  public Set<Resource> getExcludeClasses() {
    return excludeClasses;
  }

  public Set<Resource> getTopDownProperties() {
    return topDownProperties;
  }

  public Set<Resource> getBottomUpProperties() {
    return bottomUpProperties;
  }

  @Override
  public String toString() {
    return "HierarchyCheckingPayload{" +
        "includeClasses=" + includeClasses +
        ", excludeClasses=" + excludeClasses +
        ", topDownProperties=" + topDownProperties +
        ", bottomUpProperties=" + bottomUpProperties +
        '}';
  }
}
