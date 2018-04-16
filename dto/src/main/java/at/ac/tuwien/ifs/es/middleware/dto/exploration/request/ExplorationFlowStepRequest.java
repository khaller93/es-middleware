package at.ac.tuwien.ifs.es.middleware.dto.exploration.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ExplorationFlowStepRequest {

  private String name;
  private JsonNode param;

  public ExplorationFlowStepRequest(@JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "param") ObjectNode param) {
    this.name = name;
    this.param = param == null || param.isNull() ? JsonNodeFactory.instance.objectNode() : param;
  }

  public String getName() {
    return name;
  }

  public JsonNode getParameterPayload() {
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
