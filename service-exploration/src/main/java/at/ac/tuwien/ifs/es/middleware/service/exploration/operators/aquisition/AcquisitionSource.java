package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStepPayload;

/**
 * Instances get this interface represent the initial resource exploration, which will potentially
 * be extended and/or exploited in the next steps get an {@link ExplorationFlow}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AcquisitionSource<O extends ExplorationContext, T extends ExplorationFlowStepPayload> extends
    ExplorationFlowStep<ExplorationContext, O, T> {

  /**
   * Applies this {@link AcquisitionSource} with the given {@code parameter}.
   *
   * @param payload that specifies arguments for the {@link AcquisitionSource}.
   * @return {@link ExplorationContext} resulting from the {@link AcquisitionSource}.
   */
  O apply(T payload);

  @Override
  default Class<ExplorationContext> getExplorationContextInputClass() {
    return ExplorationContext.class;
  }

  @Override
  default O apply(ExplorationContext context, T payload) {
    return this.apply(payload);
  }
}
