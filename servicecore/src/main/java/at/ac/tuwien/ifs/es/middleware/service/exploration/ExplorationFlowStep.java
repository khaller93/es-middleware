package at.ac.tuwien.ifs.es.middleware.service.exploration;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.BiFunction;

/**
 * Instances of this interface represent a step of an {@link ExplorationFlow}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface ExplorationFlowStep extends
    BiFunction<ExplorationContext, JsonNode, ExplorationContext> {

  /**
   * This method returns a POJO that matches the expected parameters for this exploration step.
   *
   * @return a POJO for matching the parameters expected for this {@link ExplorationFlowStep}.
   */
  Class<?> getParameterClass();

}
