package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import sun.reflect.generics.tree.Tree;

/**
 * This is an implementation of {@link ClassInformationService} using the {@link SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
public class ClassInformationServiceImpl implements ClassInformationService {

  private static final Logger logger = LoggerFactory.getLogger(ClassInformationService.class);

  private static final String ALL_CLASSES_QUERY = "SELECT DISTINCT ?class WHERE {\n"
      + "    {_:a a ?class}\n"
      + "     UNION\n"
      + "    {?class a rdfs:Class}\n"
      + "     UNION\n"
      + "    {?class rdfs:subClassOf _:b}\n"
      + "     UNION\n"
      + "    {_:c rdfs:subClassOf ?class}\n"
      + "    FILTER(isIRI(?class)) .\n"
      + "}";

  private static final long LOAD_LIMIT = 10000;

  private static final String ALL_SUBCLASSES_QUERY = "SELECT DISTINCT ?class ?superClass WHERE {\n"
      + "    {\n"
      + "      _:a a ?class\n"
      + "    } UNION {\n"
      + "      ?class a rdfs:Class\n"
      + "    } UNION {\n"
      + "      ?class rdfs:subClassOf _:b\n"
      + "    } UNION {\n"
      + "      _:c rdfs:subClassOf ?class\n"
      + "    }\n"
      + "    ?class rdfs:subClassOf+ ?superClass .\n"
      + "    FILTER (?class != ?superClass) .\n"
      + "    FILTER NOT EXISTS {\n"
      + "  \t\t?class rdfs:subClassOf+ ?anotherSuperClass .\n"
      + "        ?anotherSuperClass rdfs:subClassOf+ ?superClass .\n"
      + "        FILTER (isIRI(?anotherClass)) .\n"
      + "    }\n"
      + "    FILTER(isIRI(?class) && isIRI(?superClass)) .\n"
      + "}\n"
      + "OFFSET %d\n"
      + "LIMIT %d";

  private static final String ALL_INSTANCE_CLASSES_QUERY =
      "SELECT DISTINCT ?resource ?class WHERE {\n"
          + "\t{?resource ?p1 _:o1}\n"
          + "     UNION\n"
          + "    {\n"
          + "     \t_:o2 ?p2 ?resource .\n"
          + "        FILTER (isIRI(?resource)) .\n"
          + "    }\n"
          + "    ?resource a/rdfs:subClassOf* ?class .\n"
          + "    FILTER (isIRI(?class)) .\n"
          + "}\n"
          + "OFFSET %d\n"
          + "LIMIT %d";

  private SPARQLService sparqlService;
  private SameAsResourceService sameAsResourceService;
  private ApplicationEventPublisher eventPublisher;
  private TaskExecutor taskExecutor;

  //TODO: replace with cache.
  private Map<ResourcePair, Set<Resource>> lcsMap = new HashMap<>();

  @Autowired
  public ClassInformationServiceImpl(
      SPARQLService sparqlService,
      SameAsResourceService sameAsResourceService,
      ApplicationEventPublisher eventPublisher,
      TaskExecutor taskExecutor) {
    this.sparqlService = sparqlService;
    this.sameAsResourceService = sameAsResourceService;
    this.eventPublisher = eventPublisher;
    this.taskExecutor = taskExecutor;
  }

  private TreeNode computeLCATreeNodeFor(Map<Resource, TreeNode> treeNodes, Resource resource) {
    if (treeNodes.containsKey(resource)) {
      return treeNodes.get(resource);
    } else {
      Set<Resource> sameAsResources = sameAsResourceService.getSameAsResourcesFor(resource);
      TreeNode treeNode = treeNodes
          .compute(resource,
              (res, node) -> node == null ? new TreeNode(res, sameAsResources) : node);
      for (Resource sameAsResource : sameAsResources) {
        treeNodes.put(sameAsResource, treeNode);
      }
      return treeNode;
    }
  }

  private Map<Resource, TreeNode> computeLCATreeNodes() {
    Map<Resource, TreeNode> treeNodes = new HashMap<>();
    /* fetch classes */
    for (Resource clazz : getAllClasses()) {
      computeLCATreeNodeFor(treeNodes, clazz);
    }
    /* fetch relationships */
    long offset = 0;
    List<Map<String, RDFTerm>> relationshipResults;
    do {
      relationshipResults = ((SelectQueryResult) sparqlService
          .query(String.format(ALL_SUBCLASSES_QUERY, offset, LOAD_LIMIT), true)).value();
      if (relationshipResults != null) {
        for (Map<String, RDFTerm> row : relationshipResults) {
          Resource classResource = new Resource((BlankNodeOrIRI) row.get("class"));
          TreeNode classTreeNode = computeLCATreeNodeFor(treeNodes, classResource);
          RDFTerm superClassRdfTerm = row.get("superClass");
          if (superClassRdfTerm != null) {
            Resource superClassResource = new Resource((BlankNodeOrIRI) superClassRdfTerm);
            TreeNode superClassTreeNode = computeLCATreeNodeFor(treeNodes, superClassResource);
            classTreeNode.addParent(superClassTreeNode);
          }
        }
        logger.info("Loaded {} class <-> superclass relationships.",
            offset + relationshipResults.size());
        offset += LOAD_LIMIT;
      } else {
        break;
      }
    } while (relationshipResults.size() == LOAD_LIMIT);
    return treeNodes;
  }

  private Map<Resource, Set<Resource>> computeResourceClasses() {
    Map<Resource, Set<Resource>> resourceClassSetMap = new HashMap<>();
    long offset = 0;
    List<Map<String, RDFTerm>> results;
    do {
      results = ((SelectQueryResult) sparqlService
          .query(String.format(ALL_INSTANCE_CLASSES_QUERY, offset, LOAD_LIMIT), true)).value();
      if (results != null) {
        for (Map<String, RDFTerm> row : results) {
          Resource resource = new Resource((BlankNodeOrIRI) row.get("resource"));
          Resource classResource = new Resource((BlankNodeOrIRI) row.get("class"));
          resourceClassSetMap.compute(resource,
              (resource1, resources) -> resources == null ? new HashSet<>() : resources)
              .add(classResource);
        }
        logger.info("Loaded {} instance <-> class relationships.", offset + results.size());
        offset += LOAD_LIMIT;
      } else {
        break;
      }
    } while (results.size() == LOAD_LIMIT);
    return resourceClassSetMap;
  }

  private Set<TreeNode> computeLCA(Map<Resource, TreeNode> treeNodeMap,
      Set<Resource> resourceAClasses, Set<Resource> resourceBClass) {
    Set<TreeNode> treeNodeSet = new HashSet<>();
    for (TreeNode treeNode : treeNodeMap.values()) {
      if (resourceAClasses.stream()
          .noneMatch(resource -> treeNode.descendantBag.contains(resource)) && resourceBClass
          .stream().noneMatch(resource -> treeNode.descendantBag.contains(resource))) {
        treeNodeSet.add(treeNode);
      }
    }
    //treeNodeSet.stream().filter();
    return treeNodeSet;
  }

  @Override
  public Void compute() {
    /* Build the class tree hierarchy. */
    Map<Resource, TreeNode> treeNodeMap = computeLCATreeNodes();
    for (TreeNode node : treeNodeMap.values()) {
      System.out.println(">>" + node.descendantBag);
    }
    /*  */
    Map<Resource, Set<Resource>> resourceClassSetMap = computeResourceClasses();
    for (Map.Entry<Resource, Set<Resource>> resourceARow : resourceClassSetMap.entrySet()) {
      for (Map.Entry<Resource, Set<Resource>> resourceBRow : resourceClassSetMap.entrySet()) {
        ResourcePair resourcePair = ResourcePair.of(resourceARow.getKey(), resourceBRow.getKey());
        lcsMap.put(resourcePair,
            computeLCA(treeNodeMap, resourceARow.getValue(), resourceBRow.getValue()).stream()
                .map(tn -> tn.resource).collect(
                Collectors.toSet()));
      }
    }
    return null;
  }

  @Cacheable("sparql")
  @Override
  public Set<Resource> getAllClasses() {
    return ((SelectQueryResult) sparqlService.query(ALL_CLASSES_QUERY, true)).value()
        .stream().map(row -> new Resource((BlankNodeOrIRI) row.get("class")))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Resource> getLeastCommonSubSumersFor(ResourcePair resourcePair) {
    return lcsMap.get(resourcePair);
  }

  @Override
  public AnalysisEventStatus getStatus() {
    return null;
  }

  /**
   * A naive implementation of a tree to solve the LCA problem.
   */
  private class TreeNode {

    private Set<TreeNode> parents = new HashSet<>();
    private Set<TreeNode> children = new HashSet<>();

    private Resource resource;
    private Set<Resource> sameAsResources;
    private Set<Resource> descendantBag = new HashSet<>();

    public TreeNode(Resource resource, Set<Resource> sameAsResources) {
      this.resource = resource;
      this.sameAsResources = sameAsResources;
      this.descendantBag.add(resource);
      this.descendantBag.addAll(sameAsResources);
    }

    private void propagateDescendants(Set<Resource> descendants) {
      Set<Resource> unknownDescendants = new HashSet<>();
      for (Resource descendant : descendants) {
        if (!descendantBag.contains(descendant)) {
          unknownDescendants.add(descendant);
        }
      }
      if (!unknownDescendants.isEmpty()) {
        descendantBag.addAll(unknownDescendants);
        for (TreeNode parent : parents) {
          parent.propagateDescendants(unknownDescendants);
        }
      }
    }

    public void addParent(TreeNode treeNode) {
      this.parents.add(treeNode);
      treeNode.children.add(this);
      propagateDescendants(this.descendantBag);
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

}
