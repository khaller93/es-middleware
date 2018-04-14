package at.ac.tuwien.ifs.es.middleware.service.exploration;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedList;
import java.util.List;
import org.javatuples.Pair;

/**
 * This class represents an exploration flow that is composed of one or more {@link
 * ExplorationFlowStep}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ExplorationFlow {

  private List<Pair<ExplorationFlowStep, JsonNode>> steps = new LinkedList<>();

  /**
   * Appends the given {@code step} to the exploration flow and pack it together with the given
   * {@code parameters} for this step.
   *
   * @param step {@link ExplorationFlowStep} that shall be appended to the flow.
   * @param parameters for the given {@link ExplorationFlowStep} in this flow.
   */
  public void appendFlowStep(ExplorationFlowStep step, JsonNode parameters) {
    this.steps.add(new Pair<>(step, parameters));
  }

  /**
   * Gets a {@link List} of {@link ExplorationFlowStep} with their parameters.
   *
   * @return a {@link List} of {@link ExplorationFlowStep} with their parameters.
   */
  public List<Pair<ExplorationFlowStep, JsonNode>> asList() {
    return new LinkedList<>(steps);
  }

  /**
   * Executes this workflow and returns the {@link ExplorationContext} of the execution, if it was
   * successful.
   *
   * @return {@link ExplorationContext} that is the result of the execution of this workflow.
   */
  public ExplorationContext execute() {
    ExplorationContext explorationContext = null;
    for (Pair<ExplorationFlowStep, JsonNode> step : steps) {
      explorationContext = step.getValue0().apply(explorationContext, step.getValue1());
    }
    return explorationContext;
  }

  @Override
  public String toString() {
    return "ExplorationFlow{" +
        "steps=" + steps +
        '}';
  }
}
