package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.request.ExplorationFlowStepRequest;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * This class is a POJO for the parameters expected by {@link at.ac.tuwien.ifs.es.middleware.service.exploration.ParallelOperator}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ParallelPayload implements ExplorationFlowStepPayload {

  @JsonProperty(value = "flows", required = true)
  private List<List<ExplorationFlowStepRequest>> flows;

  @JsonCreator
  public ParallelPayload(
      @JsonProperty(value = "flows", required = true) List<List<ExplorationFlowStepRequest>> flows) {
    this.flows = flows;
  }

  public List<List<ExplorationFlowStepRequest>> getFlows() {
    return flows;
  }

  @Override
  public String toString() {
    return "ParallelPayload{" +
        "flows=" + flows +
        '}';
  }
}
