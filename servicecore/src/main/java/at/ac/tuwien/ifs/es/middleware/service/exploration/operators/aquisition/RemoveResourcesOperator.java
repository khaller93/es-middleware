package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.RemoveResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import java.util.Collections;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionOperator} that removes resources from a given
 * {@link ResourceList}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.source.resourcelist.remove")
public class RemoveResourcesOperator implements AcquisitionOperator<RemoveResourcesPayload> {

  @Override
  public String getUID() {
    return "esm.source.resourcelist.remove";
  }

  @Override
  public Class<RemoveResourcesPayload> getParameterClass() {
    return RemoveResourcesPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, RemoveResourcesPayload payload) {
    if (context instanceof ResourceList) {
      if (!payload.getResources().isEmpty()) {
        ResourceList resourceListContext = (ResourceList) context;
        Set<Resource> resourcesToRemove =
            payload.getResources() != null ? payload.getResources() : Collections.emptySet();
        return resourceListContext.streamOfResults().filter(r -> !resourcesToRemove.contains(r))
            .collect(resourceListContext);
      }
      return context;
    } else {
      throw new IllegalArgumentException(
          "The passed context of the previous step must be a resource list.");
    }
  }
}
