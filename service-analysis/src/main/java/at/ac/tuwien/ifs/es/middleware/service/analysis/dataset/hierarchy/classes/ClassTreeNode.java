package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
class ClassTreeNode implements Serializable {

  private static final String serialVersionUID = "7841750160221275131";

  private final int id;

  private final Set<String> resources;

  private final Set<Integer> parents;
  private final Set<Integer> children;

  public ClassTreeNode(int id, Set<String> resources) {
    this.id = id;
    this.resources = resources;
    this.parents = new HashSet<>();
    this.children = new HashSet<>();
  }

  public int getId() {
    return id;
  }

  public Set<String> getResources() {
    return resources;
  }

  public void addParent(int treeNodeSuperClass) {
    parents.add(treeNodeSuperClass);
  }

  public Set<Integer> getParents() {
    return parents;
  }

  public void addChildren(int treeNodeChildrenClass) {
    children.add(treeNodeChildrenClass);
  }

  public void removeParent(int treeNodeId) {
    parents.remove(treeNodeId);
  }

  public void removeParent(Collection<Integer> treeNodeIds) {
    parents.removeAll(treeNodeIds);
  }

  public void removeChildren(int treeNodeId) {
    children.remove(treeNodeId);
  }

  public void removeChildren(Collection<Integer> treeNodeIds) {
    children.removeAll(treeNodeIds);
  }

  public Set<Integer> getChildren() {
    return children;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassTreeNode classTreeNode = (ClassTreeNode) o;
    return id == classTreeNode.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "ClassTreeNode{" +
        "id=" + id +
        ", resources=" + resources +
        ", parents=" + parents +
        ", children=" + children +
        '}';
  }
}
