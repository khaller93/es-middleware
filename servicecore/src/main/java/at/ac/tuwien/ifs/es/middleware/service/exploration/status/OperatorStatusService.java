package at.ac.tuwien.ifs.es.middleware.service.exploration.status;

import at.ac.tuwien.ifs.es.middleware.common.exploration.ExplorationFlowStep;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Instances of this service provides methods to gather information about supported {@link
 * ExplorationFlowStep}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface OperatorStatusService {

  /**
   * Returns a map of all recognized exploration flow operators. The key of the map represents the
   * category of the operators.
   *
   * @return a map of all recognized exploration flow operators.
   */
  Map<String, List<String>> getExplorationFlowOperators();

  /**
   * Gets {@link OperatorInfo} for the operator with the given {@code uid}.
   *
   * @param uid the unique id {@code uid} of the operator for which information shall be returned.
   * @return {@link OperatorInfo}, if a {@link ExplorationFlowStep}
   * with the given uid is recognized, otherwise {@link Optional#empty()}.
   */
  Optional<OperatorInfo> getExplorationFlowOperatorInfo(String uid);

}
