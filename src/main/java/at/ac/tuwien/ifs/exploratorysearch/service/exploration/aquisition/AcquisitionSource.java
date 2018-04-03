package at.ac.tuwien.ifs.exploratorysearch.service.exploration.aquisition;

import at.ac.tuwien.ifs.exploratorysearch.dto.exploration.ExplorationResponse;
import at.ac.tuwien.ifs.exploratorysearch.service.exploration.ExplorationFlow;
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
public interface AcquisitionSource extends Function<JsonNode, ExplorationResponse> {

}
