package at.ac.tuwien.ifs.es.middleware.dto.exploration.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

/**
 *
 */
public class DynamicExplorationFlowRequest {

  @JsonProperty(required = true)
  private List<ExplorationFlowStepRequest> steps;

  public static class ExplorationFlowStepRequest {

    private String name;
    private ObjectNode param;

    public ExplorationFlowStepRequest(@JsonProperty(value = "name", required = true) String name,
        @JsonProperty("param") ObjectNode param) {
      this.name = name;
      this.param = param != null ? param : JsonNodeFactory.instance.objectNode();
    }

    public String getName() {
      return name;
    }

    public ObjectNode getParameterPayload() {
      return param;
    }

    @Override
    public String toString() {
      return "ExplorationFlowStepRequest{" +
          "name='" + name + '\'' +
          ", param=" + param +
          '}';
    }
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
