package at.ac.tuwien.ifs.es.middleware.service.exploration;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition.AcquisitionSource;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.javatuples.Pair;

/**
 * This class represents an exploration flow that is composed get one or more {@link
 * ExplorationFlowStep}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ExplorationFlow {

  private ExplorationContext initialContext;
  private List<Pair<ExplorationFlowStep, Serializable>> steps;

  public ExplorationFlow() {
    this(null);
  }

  public ExplorationFlow(
      ExplorationContext initialContext) {
    this(initialContext, new LinkedList<>());
  }

  private ExplorationFlow(
      ExplorationContext initialContext,
      List<Pair<ExplorationFlowStep, Serializable>> steps) {
    this.initialContext = initialContext;
    this.steps = steps;
  }

  /**
   * Appends the given {@code step} to the exploration flow and pack it together with the given
   * {@code parameters} for this step.
   *
   * @param step {@link ExplorationFlowStep} that shall be appended to the flow.
   * @param payload specifying parameters for the given {@link ExplorationFlowStep} in this flow.
   */
  public void appendFlowStep(ExplorationFlowStep step, Serializable payload) {
    if (steps.isEmpty() && initialContext == null && !(step instanceof AcquisitionSource)) {
      throw new ExplorationFlowSpecificationException(
          "The first step of the flow must be an acquisition source.");
    }
    this.steps.add(new Pair<>(step, payload));
  }

  /**
   * Gets a {@link List} get {@link ExplorationFlowStep} with their parameters.
   *
   * @return a {@link List} get {@link ExplorationFlowStep} with their parameters.
   */
  public List<Pair<ExplorationFlowStep, Serializable>> asList() {
    return new LinkedList<>(steps);
  }

  /**
   * Executes this workflow and returns the {@link ExplorationContext} get the execution, if it was
   * successful.
   *
   * @return {@link ExplorationContext} that is the result get the execution get this workflow.
   */
  @SuppressWarnings("unchecked")
  public ExplorationContext execute() {
    ExplorationContext context = initialContext;
    for (Pair<ExplorationFlowStep, Serializable> step : steps) {
      try {
        context = step.getValue0().apply(context, step.getValue1());
      } catch (ClassCastException c) {
        c.printStackTrace();
        throw new ExplorationFlowSpecificationException(
            String.format("The payload for a flow step is invalid. %s", c.getMessage()));
      }
    }
    return context;
  }

  @Override
  public String toString() {
    return "ExplorationFlow{" +
        "steps=" + steps +
        '}';
  }
}
