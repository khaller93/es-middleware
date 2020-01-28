package at.ac.tuwien.ifs.es.middleware.service.exploration.context.hierarchy;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ExplorationContextContainer;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.IterableResourcesContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ResultCollectionContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.util.box.ValueBox;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.util.box.ValueBoxFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class HierarchyTreeContext implements IterableResourcesContext<TreeNode>,
    ResultCollectionContext<TreeNode> {

  @JsonProperty("root")
  private Set<TreeNode> rootNodes;
  @JsonProperty("all")
  private Set<TreeNode> treeNodes;

  private ValueBox values;
  private ValueBox metadata;

  public HierarchyTreeContext(@JsonProperty("all") Set<TreeNode> treeNodes,
      @JsonProperty("root") Set<TreeNode> rootNodes) {
    this(treeNodes, rootNodes, ValueBoxFactory.newBox(), ValueBoxFactory.newBox());
  }

  @JsonCreator
  public HierarchyTreeContext(
      @JsonProperty("all") Set<TreeNode> treeNodes,
      @JsonProperty("root") Set<TreeNode> rootNodes,
      @JsonProperty("values") ValueBox values,
      @JsonProperty("metadata") ValueBox metadata) {
    this.treeNodes = new HashSet<>(treeNodes);
    this.rootNodes = new HashSet<>(rootNodes);
    this.values = values;
    this.metadata = metadata;
  }

  @Override
  public Iterator<Resource> getResourceIterator() {
    return asResourceSet().iterator();
  }

  @Override
  public List<Resource> asResourceList() {
    return treeNodes.stream().map(TreeNode::getResource).collect(Collectors.toList());
  }

  @Override
  public Set<Resource> asResourceSet() {
    return treeNodes.stream().map(TreeNode::getResource).collect(Collectors.toSet());
  }

  @Override
  public ValueBox values() {
    return values;
  }

  @Override
  public ValueBox metadata() {
    return metadata;
  }

  @Override
  public Stream<TreeNode> streamOfResults() {
    return treeNodes.stream();
  }

  @Override
  public Collector<TreeNode, ? extends ExplorationContextContainer<TreeNode>, ? extends ResultCollectionContext<TreeNode>> collector() {
    return null;
  }

  @Override
  public void add(TreeNode entry) {
    treeNodes.add(entry);
  }

  @Override
  public void remove(TreeNode entry) {
    treeNodes.remove(entry);
  }

  @Override
  public long size() {
    return treeNodes.size();
  }

  @NotNull
  @Override
  public Iterator<TreeNode> iterator() {
    return treeNodes.iterator();
  }
}
