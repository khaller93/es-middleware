package at.ac.tuwien.ifs.es.middleware.service.exploration.factory;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.request.DynamicExplorationFlowRequest;
import at.ac.tuwien.ifs.es.middleware.service.exploration.request.ExplorationFlowStepRequest;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.common.exploration.ExplorationFlowStep;
import at.ac.tuwien.ifs.es.middleware.common.exploration.ExplorationFlowStepPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * This factory makes use of the central {@link DynamicExplorationFlowFactory} to read requests from
 * clients and constructs the corresponding {@link ExplorationFlow}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class DynamicExplorationFlowFactory {

  private static final Logger logger = LoggerFactory.getLogger(DynamicExplorationFlowFactory.class);

  private ApplicationContext context;
  private ExplorationFlowRegistry registry;
  private ObjectMapper payloadMapper;

  @Autowired
  public DynamicExplorationFlowFactory(ApplicationContext context,
      ExplorationFlowRegistry registry, ObjectMapper payloadMapper) {
    this.context = context;
    this.registry = registry;
    this.payloadMapper = payloadMapper;
  }

  /**
   * Takes the specification of an {@link ExplorationFlow} written by a client and constructs the
   * corresponding exploration flow.
   *
   * @param request that was specified by the client.
   * @return {@link ExplorationFlow} that covers the specification of the client.
   */
  public ExplorationFlow constructFlow(DynamicExplorationFlowRequest request) {
    logger.debug("Start to dynamically construct the flow '{}'.", request);
    return constructFlow(request.getSteps());
  }

  /**
   * Takes the specification of a complete {@link ExplorationFlow} written by a client and
   * constructs the corresponding exploration flow.
   *
   * @param steps that were specified by the client.
   * @return {@link ExplorationFlow} that covers the specification of the client.
   */
  public ExplorationFlow constructFlow(List<ExplorationFlowStepRequest> steps) {
    return constructFlow(null, steps);
  }

  /**
   * Takes a partial specification of an {@link ExplorationFlow} written by a client and constructs
   * the corresponding exploration flow.
   *
   * @param initialContext initial {@link ExplorationContext} that can be null.
   * @param steps that were specified by the client.
   * @return {@link ExplorationFlow} that covers the specification get the client.
   */
  public ExplorationFlow constructFlow(ExplorationContext initialContext,
      List<ExplorationFlowStepRequest> steps) {
    logger.debug("Start to dynamically construct the flow with steps '{}'.", steps);
    ExplorationFlow flow = new ExplorationFlow(initialContext);
    for (ExplorationFlowStepRequest step : steps) {
      Optional<Class<? extends ExplorationFlowStep>> optionalClass = registry.get(step.getName());
      if (optionalClass.isPresent()) {
        try {
          ExplorationFlowStep stepObject = context.getBean(optionalClass.get());
          flow.appendFlowStep(stepObject, (ExplorationFlowStepPayload) payloadMapper
              .treeToValue(step.getParameterPayload(), stepObject.getPayloadClass()));
        } catch (JsonProcessingException j) {
          throw new ExplorationFlowSpecificationException(String
              .format("The payload for exploration flow step '%s' is invalid. %s", step.getName(),
                  j.getMessage()), j);
        }
      } else {
        throw new ExplorationFlowSpecificationException(
            String.format("There is no exploration flow operator registered under the name '%s'.",
                step.getName()));
      }
    }
    return flow;
  }

}
