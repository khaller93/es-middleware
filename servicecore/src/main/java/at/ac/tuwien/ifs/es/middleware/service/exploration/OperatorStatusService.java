package at.ac.tuwien.ifs.es.middleware.service.exploration;

import at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation.AggregationOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition.AcquisitionOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition.AcquisitionSource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.exploitation.ExploitationOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service allows the client to analyze the provided operators.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class OperatorStatusService {

  private ExplorationFlowRegistry explorationFlowRegistry;

  @Autowired
  public OperatorStatusService(ExplorationFlowRegistry explorationFlowRegistry) {
    this.explorationFlowRegistry = explorationFlowRegistry;
  }

  /**
   * Looks for all registered {@link at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow}
   * for this middleware and checks whether it is a {@link AcquisitionSource}, {@link
   * AcquisitionOperator}, {@link ExploitationOperator} and {@link AggregationOperator}.
   * <p/>
   * Then it returns a map, where the key is the type of operator {@code source}, {@code
   * acquisition}, {@code exploitation}, and {@code aggregation}.
   *
   * @return the map of registered operators, where the key is the type.
   */
  public Map<String, List<String>> getExplorationFlowOperators() {
    Map<String, List<String>> returnMap = new HashMap<>();
    Map<String, Class<? extends ExplorationFlowStep>> allRegisteredSteps = explorationFlowRegistry
        .getAllRegisteredSteps();
    for (Entry<String, Class<? extends ExplorationFlowStep>> entry : allRegisteredSteps
        .entrySet()) {
      String type = "unknown";
      Class<? extends ExplorationFlowStep> clazz = entry.getValue();
      if (AcquisitionSource.class.isAssignableFrom(clazz)) {
        type = "source";
      } else if (AcquisitionOperator.class.isAssignableFrom(clazz)) {
        type = "acquisition";
      } else if (ExploitationOperator.class.isAssignableFrom(clazz)) {
        type = "exploitation";
      } else if (AggregationOperator.class.isAssignableFrom(clazz)) {
        type = "aggregation";
      }
      List<String> opList = returnMap.compute(type,
          (s, oldOperatorList) -> oldOperatorList == null ? new LinkedList<>() : oldOperatorList);
      opList.add(entry.getKey());
    }
    return returnMap;
  }
}
