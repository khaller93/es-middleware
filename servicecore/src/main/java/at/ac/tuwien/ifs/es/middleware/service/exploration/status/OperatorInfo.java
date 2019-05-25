package at.ac.tuwien.ifs.es.middleware.service.exploration.status;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This dto maintains information about a {@link at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class OperatorInfo {

  private String uid;
  private String inputType;
  private String outputType;
  private JsonNode parameters;

  public OperatorInfo(String uid, String inputType, String outputType,
      JsonNode parameters) {
    this.uid = uid;
    this.inputType = inputType;
    this.outputType = outputType;
    this.parameters = parameters;
  }

  public String getUid() {
    return uid;
  }

  public String getInputType() {
    return inputType;
  }

  public String getOutputType() {
    return outputType;
  }

  public JsonNode getParameters() {
    return parameters;
  }

  @Override
  public String toString() {
    return "OperatorInfo{" +
        "uid='" + uid + '\'' +
        ", inputType='" + inputType + '\'' +
        ", outputType='" + outputType + '\'' +
        ", parameters=" + parameters +
        '}';
  }
}
