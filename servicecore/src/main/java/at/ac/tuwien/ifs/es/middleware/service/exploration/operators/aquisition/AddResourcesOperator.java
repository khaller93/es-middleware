package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.AddResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionOperator} that adds resources to a {@link
 * ResourceList}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.resourcelist.add")
public class AddResourcesOperator implements AcquisitionOperator<AddResourcesPayload> {

  @Override
  public String getUID() {
    return "esm.source.resourcelist.add";
  }

  @Override
  public Class<AddResourcesPayload> getParameterClass() {
    return AddResourcesPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, AddResourcesPayload payload) {
    if (context instanceof ResourceList) {
      if (!payload.getResources().isEmpty()) {
        ResourceList resourceListContext = (ResourceList) context;
        List<Resource> resourcesToAdd =
            payload.getResources() != null ? payload.getResources() : Collections.emptyList();
        if (payload.getNodup()) {
          Set<Resource> resourceSet = resourceListContext.asResourceSet();
          resourcesToAdd = resourcesToAdd.stream().filter(r -> !resourceSet.contains(r))
              .collect(Collectors.toList());
        }
        List<Resource> resourceList = resourceListContext.asResourceList();
        if (payload.getIndex() == -1) {
          resourceList.addAll(resourcesToAdd);
        } else {
          resourceList.addAll(payload.getIndex(), resourcesToAdd);
        }
        return resourceList.stream().collect(resourceListContext);
      } else {
        return context;
      }
    } else {
      throw new IllegalArgumentException(
          "The passed context of the previous step must be a resource list.");
    }
  }
}
