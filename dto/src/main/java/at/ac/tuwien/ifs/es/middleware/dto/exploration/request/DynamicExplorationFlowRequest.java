package at.ac.tuwien.ifs.es.middleware.dto.exploration.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 */
public class DynamicExplorationFlowRequest {

  private List<ExplorationFlowStepRequest> steps;

  public DynamicExplorationFlowRequest(
      @JsonProperty(value = "steps", required = true) List<ExplorationFlowStepRequest> steps) {
    this.steps = steps;
  }

  public List<ExplorationFlowStepRequest> getSteps() {
    return steps;
  }

  @Override
  public String toString() {
    return "DynamicExplorationFlowRequest{" +
        "steps=" + steps +
        '}';
  }
}
