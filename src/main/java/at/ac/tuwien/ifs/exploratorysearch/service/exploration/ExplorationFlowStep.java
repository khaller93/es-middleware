package at.ac.tuwien.ifs.exploratorysearch.service.exploration;

import at.ac.tuwien.ifs.exploratorysearch.dto.exploration.ExplorationResponse;
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
    BiFunction<ExplorationResponse, JsonNode, ExplorationResponse> {

}
