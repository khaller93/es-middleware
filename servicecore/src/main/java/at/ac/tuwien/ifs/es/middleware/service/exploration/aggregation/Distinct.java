package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.payload.VoidPayload;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class Distinct implements AggregationOperator<VoidPayload> {

  private static final Logger logger = LoggerFactory.getLogger(Distinct.class);

  private SameAsResourceService sameAsResourceService;

  @Autowired
  public Distinct(SameAsResourceService sameAsResourceService) {
    this.sameAsResourceService = sameAsResourceService;
  }

  @Override
  public Class<VoidPayload> getParameterClass() {
    return VoidPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, VoidPayload payload) {
    logger.debug("Apply distinct operation to context {}", context);
    if (context instanceof ResourceList) {
      ResourceList resourceList = (ResourceList) context;
      List<Resource> newResourceList = new LinkedList<>();
      Set<Resource> recognizedResources = new HashSet<>();
      for (Resource resource : resourceList) {
        if (!recognizedResources.contains(resource)) {
          newResourceList.add(resource);
          recognizedResources.add(resource);
          recognizedResources.addAll(sameAsResourceService.getSameAsResourcesFor(resource));
        }
      }
      return newResourceList.stream().collect(resourceList);
    } else {
      throw new ExplorationFlowSpecificationException(
          "The given exploration flow is not valid, because a distinct operator needs a resource list context.");
    }
  }
}
