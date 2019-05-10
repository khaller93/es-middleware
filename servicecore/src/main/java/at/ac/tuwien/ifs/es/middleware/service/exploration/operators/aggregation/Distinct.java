package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceCollection;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.VoidPayload;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an {@link AggregationOperator} that eliminates duplicates in an {@link
 * at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList}. Duplicates are resources
 * which hold an {@code owl:sameAs} relationship.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.aggregate.distinct")
public class Distinct implements
    AggregationOperator<ResourceCollection, ResourceCollection, VoidPayload> {

  private SameAsResourceService sameAsResourceService;

  @Autowired
  public Distinct(SameAsResourceService sameAsResourceService) {
    this.sameAsResourceService = sameAsResourceService;
  }

  @Override
  public String getUID() {
    return "esm.aggregate.distinct";
  }

  @Override
  public Class<ResourceCollection> getExplorationContextInputClass() {
    return ResourceCollection.class;
  }

  @Override
  public Class<ResourceCollection> getExplorationContextOutputClass() {
    return ResourceCollection.class;
  }

  @Override
  public Class<VoidPayload> getPayloadClass() {
    return VoidPayload.class;
  }

  @Override
  public ResourceCollection apply(ResourceCollection resourceCollection, VoidPayload payload) {
    List<Resource> newResourceList = new LinkedList<>();
    Set<Resource> recognizedResources = new HashSet<>();
    for (Resource resource : resourceCollection) {
      if (!recognizedResources.contains(resource)) {
        newResourceList.add(resource);
        recognizedResources.add(resource);
        recognizedResources.addAll(sameAsResourceService.getSameAsResourcesFor(resource));
      }
    }
    return (ResourceCollection) newResourceList.stream().collect(resourceCollection);
  }
}
