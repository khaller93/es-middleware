package at.ac.tuwien.ifs.es.middleware.service.exploration.context.hierarchy;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Identifiable;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import java.util.List;
import java.util.Objects;

public class TreeNode implements Identifiable {

  private Resource resource;
  private List<Resource> parents;
  private List<Resource> children;

  public TreeNode(Resource resource, List<Resource> parents, List<Resource> children) {
    this.resource = resource;
    this.parents = parents;
    this.children = children;
  }

  @Override
  public String getId() {
    return resource.getId();
  }

  public Resource getResource() {
    return resource;
  }

  public List<Resource> getChildren() {
    return children;
  }

  public List<Resource> getParents() {
    return parents;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TreeNode treeNode = (TreeNode) o;
    return Objects.equals(resource, treeNode.resource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resource);
  }
}
