package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IterableResourcesContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourcePairList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.PairingPayload;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.factory.DynamicExplorationFlowFactory;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.source.pairing")
public class ResourcePairing implements AcquisitionOperator<PairingPayload> {

  private static final Logger logger = LoggerFactory.getLogger(ResourcePairing.class);

  private DynamicExplorationFlowFactory dynamicExplorationFlowFactory;

  @Autowired
  public ResourcePairing(DynamicExplorationFlowFactory dynamicExplorationFlowFactory) {
    this.dynamicExplorationFlowFactory = dynamicExplorationFlowFactory;
  }

  @Override
  public String getUID() {
    return "esm.source.pairing";
  }

  @Override
  public Class<PairingPayload> getParameterClass() {
    return PairingPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, PairingPayload payload) {
    if (context instanceof IterableResourcesContext) {
      IterableResourcesContext source = (IterableResourcesContext) context;
      ExplorationContext oTarget = dynamicExplorationFlowFactory.constructFlow(payload.getSteps())
          .execute();
      if (oTarget instanceof IterableResourcesContext) {
        IterableResourcesContext target = (IterableResourcesContext) oTarget;
        List<ResourcePair> resourcePairs = new LinkedList<>();
        Set<Resource> targetResources = target.asResourceSet();
        for (Resource sourceR : source.asResourceSet()) {
          for (Resource targetR : targetResources) {
            if (payload.isSelfReflectionAllowed() || !sourceR.equals(targetR)) {
              resourcePairs.add(ResourcePair.of(sourceR, targetR));
            }
          }
          if (payload.isSymmetric()) {
            targetResources.remove(sourceR);
          }
        }
        ResourcePairList pairedContext = new ResourcePairList(resourcePairs);
        // merge values
        pairedContext.mergeValues(context.getAllValues());
        pairedContext.mergeValues(oTarget.getAllValues());
        // merge metadata
        pairedContext.mergeMetadata(context.getMetadata());
        pairedContext.mergeMetadata(oTarget.getMetadata());
        return pairedContext;
      } else {
        throw new ExplorationFlowSpecificationException(String.format(
            "The result of the specified steps must allow to iterate over resources, but for %s this is not the case.",
            oTarget.getClass().getSimpleName()));
      }
    } else {
      throw new ExplorationFlowSpecificationException(String.format(
          "The result of the previous step must allow to iterate over resources, but for %s this is not the case.",
          context.getClass().getSimpleName()));
    }
  }
}
