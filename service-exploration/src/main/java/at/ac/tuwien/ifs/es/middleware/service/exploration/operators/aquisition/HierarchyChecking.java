package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.general.ResourceHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.general.ResourceNode;
import at.ac.tuwien.ifs.es.middleware.service.exploration.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ResultCollectionContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.hierarchy.HierarchyTreeContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.hierarchy.TreeNode;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.HierarchyCheckingPayload;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
@RegisterForExplorationFlow(HierarchyChecking.OID)
public class HierarchyChecking implements
    AcquisitionSource<ResultCollectionContext, HierarchyCheckingPayload> {

  private static final Logger logger = LoggerFactory.getLogger(HierarchyChecking.class);

  public static final String OID = "esm.source.hierarchy.checking";

  private final ResourceHierarchyService resourceHierarchyService;

  @Autowired
  public HierarchyChecking(
      ResourceHierarchyService resourceHierarchyService) {
    this.resourceHierarchyService = resourceHierarchyService;
  }

  @Override
  public String getUID() {
    return OID;
  }

  @Override
  public Class<ResultCollectionContext> getExplorationContextOutputClass() {
    return ResultCollectionContext.class;
  }

  @Override
  public Class<HierarchyCheckingPayload> getPayloadClass() {
    return HierarchyCheckingPayload.class;
  }

  private TreeNode transform(ResourceNode resourceNode) {
    return new TreeNode(resourceNode.getResource(),
        resourceNode.getParentResources().stream().map(ResourceNode::getResource).collect(
            Collectors.toList()),
        resourceNode.getChildResources().stream().map(ResourceNode::getResource).collect(
            Collectors.toList()));
  }

  @Override
  public ResultCollectionContext apply(HierarchyCheckingPayload payload) {
    List<ResourceNode> tree = resourceHierarchyService
        .getHierarchy(payload.getIncludeClasses(), payload.getExcludeClasses(),
            payload.getTopDownProperties(), payload.getBottomUpProperties());
    return new HierarchyTreeContext(tree.stream().map(this::transform).collect(Collectors.toSet()),
        tree.stream().filter(r -> r.getParentResources().isEmpty()).map(this::transform)
            .collect(Collectors.toSet()));
  }

}
