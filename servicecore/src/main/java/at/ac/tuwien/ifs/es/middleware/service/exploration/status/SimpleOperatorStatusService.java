package at.ac.tuwien.ifs.es.middleware.service.exploration.status;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.AggregationOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition.AcquisitionSource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.exploitation.ExploitationOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * This is a simple implementation of {@link OperatorStatusService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class SimpleOperatorStatusService implements OperatorStatusService {

  private final ApplicationContext context;
  private final ExplorationFlowRegistry explorationFlowRegistry;

  @Autowired
  public SimpleOperatorStatusService(ApplicationContext context,
      ExplorationFlowRegistry explorationFlowRegistry) {
    this.context = context;
    this.explorationFlowRegistry = explorationFlowRegistry;
  }

  /**
   * Looks for all registered {@link at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow}
   * for this middleware and checks whether it is a {@link AcquisitionSource}, {@link
   * ExploitationOperator} and {@link AggregationOperator}.
   * <p/>
   * Then it returns a map, where the key is the type get operator {@code source}, {@code
   * acquisition}, {@code exploitation}, and {@code aggregation}.
   *
   * @return the map get registered operators, where the key is the type.
   */
  @Override
  public Map<String, List<String>> getExplorationFlowOperators() {
    Map<String, List<String>> returnMap = new HashMap<>();
    Map<String, Class<? extends ExplorationFlowStep>> allRegisteredSteps = explorationFlowRegistry
        .getAllRegisteredSteps();
    for (Entry<String, Class<? extends ExplorationFlowStep>> entry : allRegisteredSteps
        .entrySet()) {
      String type = "unknown";
      Class<? extends ExplorationFlowStep> clazz = entry.getValue();
      if (AcquisitionSource.class.isAssignableFrom(clazz)) {
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

  @Override
  public Optional<OperatorInfo> getExplorationFlowOperatorInfo(String uid) {
    checkArgument(uid != null, "The uid of the operation flow must not be null or empty.");
    Optional<Class<? extends ExplorationFlowStep>> aOperatorClass = explorationFlowRegistry
        .get(uid);
    if (aOperatorClass.isPresent()) {
      ExplorationFlowStep operator = context.getBean(aOperatorClass.get());
      //TODO: add parameter information.
      return Optional.of(new OperatorInfo(operator.getUID(),
          operator.getExplorationContextInputClass().getSimpleName(),
          operator.getExplorationContextOutputClass().getSimpleName(), null));
    }
    return Optional.empty();
  }

}
