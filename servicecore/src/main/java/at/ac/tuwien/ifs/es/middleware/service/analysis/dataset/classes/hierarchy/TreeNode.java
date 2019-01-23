package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The class hierarchy.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
class TreeNode {

  private Set<Resource> resources;

  private Set<TreeNode> parents = new HashSet<>();
  private Set<TreeNode> children = new HashSet<>();

  private Set<Resource> descendantBag = new HashSet<>();

  public TreeNode(Set<Resource> resources) {
    checkArgument(resources != null,
        "At least one class resource must be specified for a treenode.");
    this.resources = resources;
  }

  public Set<Resource> getClassResources() {
    return resources;
  }

  public Set<Resource> getDescendantBag() {
    return descendantBag;
  }

  public void addChildren(TreeNode childNode) {
    checkArgument(childNode != null, "");
    this.children.add(childNode);
    /* add descendants to the bag */
    Set<Resource> classResources = new HashSet<>(childNode.getClassResources());
    classResources.addAll(childNode.getDescendantBag());

  }

}
