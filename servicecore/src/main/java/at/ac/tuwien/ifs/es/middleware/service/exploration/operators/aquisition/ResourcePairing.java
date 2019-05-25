package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.IterableResourcesContext;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources.ResourceCollection;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources.pairs.ResourcePairList;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.exploitation.ExploitationOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.PairingPayload;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.factory.DynamicExplorationFlowFactory;
import at.ac.tuwien.ifs.es.middleware.common.exploration.RegisterForExplorationFlow;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
@RegisterForExplorationFlow(ResourcePairing.OID)
public class ResourcePairing implements
    ExploitationOperator<ResourceCollection, ResourcePairList, PairingPayload> {

  public static final String OID = "esm.source.pairing";

  private DynamicExplorationFlowFactory dynamicExplorationFlowFactory;

  @Autowired
  public ResourcePairing(DynamicExplorationFlowFactory dynamicExplorationFlowFactory) {
    this.dynamicExplorationFlowFactory = dynamicExplorationFlowFactory;
  }

  @Override
  public String getUID() {
    return OID;
  }

  @Override
  public Class<ResourceCollection> getExplorationContextInputClass() {
    return ResourceCollection.class;
  }

  @Override
  public Class<ResourcePairList> getExplorationContextOutputClass() {
    return ResourcePairList.class;
  }

  @Override
  public Class<PairingPayload> getPayloadClass() {
    return PairingPayload.class;
  }

  @Override
  public ResourcePairList apply(ResourceCollection source, PairingPayload payload) {
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
      pairedContext.values().merge(source.values());
      pairedContext.values().merge(oTarget.values());
      // merge metadata
      pairedContext.metadata().merge(source.metadata());
      pairedContext.metadata().merge(oTarget.metadata());
      return pairedContext;
    } else {
      throw new ExplorationFlowSpecificationException(String.format(
          "The result of the specified steps must allow to iterate over resources, but for %s this is not the case.",
          oTarget.getClass().getSimpleName()));
    }
  }
}
