package at.ac.tuwien.ifs.es.middleware.service.exploration.request;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * This class represents a dynamic exploration flow, which consists of a number of steps executing a
 * registered operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class DynamicExplorationFlowRequest {

  private List<ExplorationFlowStepRequest> steps;

  @JsonCreator
  public DynamicExplorationFlowRequest(
      @JsonProperty(value = "steps", required = true) List<ExplorationFlowStepRequest> steps) {
    checkArgument(steps != null && !steps.isEmpty(),
        "A flow must have one or more steps specified.");
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
