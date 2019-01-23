package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ClassHierarchyTree {

  private Map<Resource, TreeNode> treeNodeMap = new HashMap<>();

  /**
   * Registers the given class {@link Resource}.
   *
   * @param classResource the class {@link Resource} that shall be registered.
   * @param sameAsResources a list of class {@link Resource}s that are the (owl:) same as the given
   * class.
   */
  public void registerClass(Resource classResource, Set<Resource> sameAsResources) {
    checkArgument(classResource != null, "A class resource for a tree node must not be null.");
    /* combine all sameAs classes */
    Set<Resource> classResources =
        sameAsResources != null ? new HashSet<>(sameAsResources) : new HashSet<>();
    classResources.add(classResource);
    /* find or create treenode */
    TreeNode tn = null;
    for (Resource clazz : classResources) {
      if (treeNodeMap.containsKey(clazz)) {
        tn = treeNodeMap.get(clazz);
        break;
      }
    }
    if (tn == null) {
      tn = new TreeNode(classResources);
    }
    /* push it to the map */
    treeNodeMap.put(classResource, tn);
  }

  public void addConnection(Resource subClass, Resource superClass){

  }

}
