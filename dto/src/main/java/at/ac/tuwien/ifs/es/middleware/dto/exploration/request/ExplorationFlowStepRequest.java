package at.ac.tuwien.ifs.es.middleware.dto.exploration.request;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class represents one step in a exploration flow. A step must have a name referencing to a
 * registered operator and optionally specifies arguments for the execution of the operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ExplorationFlowStepRequest {

  private String name;
  private JsonNode param;

  @JsonCreator
  public ExplorationFlowStepRequest(@JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "param") ObjectNode param) {
    checkArgument(name != null && !name.isEmpty(),
        "The name of the step must be specified and not be empty.");
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
