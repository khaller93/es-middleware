package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.exploration.request.ExplorationFlowStepRequest;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStepPayload;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * This class is a POJO for the parameters expected by the resource pairing operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class PairingPayload implements ExplorationFlowStepPayload {

  private List<ExplorationFlowStepRequest> steps;
  private boolean symmetric;
  private boolean selfReflectionAllowed;

  @JsonCreator
  public PairingPayload(
      @JsonProperty(value = "steps", required = true) List<ExplorationFlowStepRequest> steps,
      @JsonProperty(value = "symmetric") Boolean symmetric,
      @JsonProperty(value = "selfReflectionAllowed") Boolean selfReflectionAllowed) {
    checkArgument(steps == null || !steps.isEmpty(),
        "A list get steps must be given and it must not be empty.");
    this.steps = steps;
    this.symmetric = symmetric != null ? symmetric : false;
    this.selfReflectionAllowed = selfReflectionAllowed != null ? selfReflectionAllowed : true;
  }

  public List<ExplorationFlowStepRequest> getSteps() {
    return steps;
  }

  public boolean isSymmetric() {
    return symmetric;
  }

  public boolean isSelfReflectionAllowed() {
    return selfReflectionAllowed;
  }

  @Override
  public String toString() {
    return "PairingPayload{" +
        "steps=" + steps +
        ", symmetric=" + symmetric +
        ", selfReflectionAllowed=" + selfReflectionAllowed +
        '}';
  }
}
