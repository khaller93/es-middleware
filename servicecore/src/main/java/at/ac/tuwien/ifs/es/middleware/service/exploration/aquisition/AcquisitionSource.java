package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.Function;

/**
 * Instances of this interface represent the initial resource exploration, which will potentially be
 * extended and/or exploited in the next steps of an {@link ExplorationFlow}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AcquisitionSource extends ExplorationFlowStep {

  /**
   * Applies this {@link AcquisitionSource} with the given {@code parameterMap}.
   *
   * @param parameterMap that specifies arguments for the {@link AcquisitionSource}.
   * @return {@link ExplorationContext} resulting from the {@link AcquisitionSource}.
   */
  ExplorationContext apply(JsonNode parameterMap);

  @Override
  default ExplorationContext apply(ExplorationContext explorationContext,
      JsonNode parameterMap) {
    return this.apply(parameterMap);
  }
}
