package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;

/**
 * The class hierarchy.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
class CTreeNode {

  private Resource resource;
  private Set<Resource> sameAsResources;

  private Set<CTreeNode> parents = new HashSet<>();
  private Set<CTreeNode> children = new HashSet<>();

  private Set<Resource> descendantBag = new HashSet<>();

  public CTreeNode(Resource classResource, Set<Resource> sameAsClasses) {
    checkArgument(classResource != null, "A class resource for a tree node must not be null.");
    this.resource = classResource;
    this.sameAsResources = sameAsClasses != null ? sameAsClasses : Sets.newHashSet();
  }

  public Resource getMainClassResource() {
    return resource;
  }

  public Set<Resource> getClassResources() {
    Set<Resource> classResources = new HashSet<>(sameAsResources);
    classResources.add(resource);
    return classResources;
  }

  public Set<Resource> getDescendantBag() {
    return descendantBag;
  }

  public void addChildren(CTreeNode childNode) {
    checkArgument(childNode != null, "");
    this.children.add(childNode);
    /* add descendants to the bag */
    Set<Resource> classResources = new HashSet<>(childNode.getClassResources());
    classResources.addAll(childNode.getDescendantBag());

  }

}
