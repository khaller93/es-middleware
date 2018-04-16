package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import java.io.Serializable;

/**
 * Instances of this interface represent the initial resource exploration, which will potentially be
 * extended and/or exploited in the next steps of an {@link ExplorationFlow}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AcquisitionSource<T extends Serializable> extends ExplorationFlowStep<T> {

  /**
   * Applies this {@link AcquisitionSource} with the given {@code parameter}.
   *
   * @param payload that specifies arguments for the {@link AcquisitionSource}.
   * @return {@link ExplorationContext} resulting from the {@link AcquisitionSource}.
   */
  ExplorationContext apply(T payload);

  @Override
  default ExplorationContext apply(ExplorationContext context, T payload) {
    return this.apply(payload);
  }
}
