package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.general;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceNode {

  private final Resource resource;
  private Set<ResourceNode> parentResources;
  private Set<ResourceNode> childResources;

  public ResourceNode(Resource resource) {
    this(resource, new HashSet<>(), new HashSet<>());
  }

  public ResourceNode(Resource resource,
      Set<ResourceNode> parentResources,
      Set<ResourceNode> childResources) {
    checkArgument(resource != null, "The passed resource must not be null.");
    this.resource = resource;
    this.parentResources =
        parentResources != null ? new HashSet<>(parentResources) : new HashSet<>();
    this.childResources = childResources != null ? new HashSet<>(childResources) : new HashSet<>();
  }

  public Resource getResource() {
    return resource;
  }

  public Set<ResourceNode> getParentResources() {
    return new HashSet<>(parentResources);
  }

  public void addParentResourceNode(ResourceNode node) {
    parentResources.add(node);
  }

  public void removeAllParents(List<ResourceNode> parents) {
    parentResources.removeAll(parents);
  }

  public Set<ResourceNode> getChildResources() {
    return new HashSet<>(childResources);
  }

  public void addChildResourceNode(ResourceNode node) {
    childResources.add(node);
  }

  public void removeAllChildren(List<ResourceNode> children) {
    childResources.removeAll(children);
  }

  public void removeChild(ResourceNode child) {
    childResources.remove(child);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceNode that = (ResourceNode) o;
    return Objects.equals(resource, that.resource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resource);
  }

  @Override
  public String toString() {
    return "ResourceNode{" +
        "resource=" + resource +
        ", parentResources=" + parentResources.stream().map(ResourceNode::getResource)
        .collect(Collectors.toList()) +
        ", childResources=" + childResources.stream().map(ResourceNode::getResource)
        .collect(Collectors.toList()) +
        '}';
  }
}
