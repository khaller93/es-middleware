package at.ac.tuwien.ifs.exploratorysearch.service.exploration;

import at.ac.tuwien.ifs.exploratorysearch.dto.exploration.ExplorationResponse;
import at.ac.tuwien.ifs.exploratorysearch.service.exploration.aquisition.AcquisitionSource;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedList;
import java.util.List;


/**
 * This class represents an exploration flow that is composed of one or more {@link
 * ExplorationFlowStep}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ExplorationFlow {

  private AcquisitionSource acquisitionSource;
  private List<ExplorationFlowStep> explorationFlowSteps = new LinkedList<>();

  public ExplorationFlow(AcquisitionSource acquisitionSource) {
    this.acquisitionSource = acquisitionSource;
  }

  /**
   *
   * @param parameterMap
   * @return
   */
  public ExplorationResponse execute(JsonNode parameterMap) {
    ExplorationResponse result = this.acquisitionSource.apply(parameterMap);
    for (ExplorationFlowStep step : explorationFlowSteps) {
      result = step.apply(result, parameterMap);
    }
    return result;
  }


}
