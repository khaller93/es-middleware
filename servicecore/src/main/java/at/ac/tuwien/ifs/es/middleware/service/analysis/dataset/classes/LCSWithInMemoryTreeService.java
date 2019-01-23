package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation get {@link LeastCommonSubsumersService} that uses the {@link
 * SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = LCSWithInMemoryTreeService.LCS_UID, requiresSPARQL = true, prerequisites = {
    AllClassesService.class, SameAsResourceService.class})
public class LCSWithInMemoryTreeService implements LeastCommonSubsumersService {

  private static final Logger logger = LoggerFactory.getLogger(LeastCommonSubsumersService.class);

  public static final String LCS_UID = "esm.service.analytics.dataset.lcs.tree";

  private static final long LOAD_LIMIT = 100000L;

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

  private final SPARQLService sparqlService;
  private final AllClassesService allClassesService;
  private final SameAsResourceService sameAsResourceService;
  private final DB mapDB;

  private final BTreeMap<Object[], Set<String>> subsumerMap;

  @Autowired
  public LCSWithInMemoryTreeService(
      SPARQLService sparqlService,
      AllClassesService allClassesService,
      SameAsResourceService sameAsResourceService,
      DB mapDB) {
    this.sparqlService = sparqlService;
    this.allClassesService = allClassesService;
    this.sameAsResourceService = sameAsResourceService;
    this.mapDB = mapDB;
    this.subsumerMap = mapDB
        .treeMap(LCS_UID, new SerializerArrayTuple(Serializer.STRING, Serializer.STRING),
            Serializer.JAVA).createOrOpen();
  }

  private static Object[] simKey(ResourcePair resourcePair) {
    return new Object[]{resourcePair.getFirst().getId(), resourcePair.getSecond().getId()};
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
    for (Resource clazz : allClassesService.getAllClasses()) {
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
        logger.info("Loaded {} (class <-> superclass) relationships.",
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

  private Set<TreeNode> computeLCAForClass(TreeNode currentNode, Set<Resource> resourceBClasses) {
    if (currentNode == null) {
      return Sets.newHashSet();
    } else if (resourceBClasses.stream()
        .anyMatch(resB -> currentNode.descendantBag.contains(resB))) {
      return Sets.newHashSet(currentNode);
    } else {
      Set<TreeNode> lcsSet = new HashSet<>();
      for (TreeNode parent : currentNode.parents) {
        lcsSet.addAll(computeLCAForClass(parent, resourceBClasses));
      }
      return lcsSet;
    }
  }

  private Set<Resource> computeLCA(Map<Resource, TreeNode> treeNodeMap,
      Set<Resource> resourceAClasses, Set<Resource> resourceBClasses) {
    Set<TreeNode> treeNodeSet = new HashSet<>();
    for (Resource classA : resourceAClasses) {
      treeNodeSet.addAll(computeLCAForClass(treeNodeMap.get(classA), resourceBClasses));
    }
    Set<Resource> lcsSet = new HashSet<>();
    for (TreeNode treeNode : treeNodeSet) {
      if (treeNodeSet.stream().filter(t -> !t.equals(treeNode)).map(t -> t.resource)
          .noneMatch(res -> treeNode.descendantBag.contains(res))) {
        lcsSet.add(treeNode.resource);
      }
    }
    return lcsSet;
  }

  @Override
  public void compute() {
    /* build the class tree hierarchy. */
    Map<Resource, TreeNode> treeNodeMap = computeLCATreeNodes();
    /* compute the least common subsumer */
    Map<Resource, Set<Resource>> resourceClassSetMap = computeResourceClasses();
    for (Map.Entry<Resource, Set<Resource>> resourceARow : resourceClassSetMap.entrySet()) {
      for (Map.Entry<Resource, Set<Resource>> resourceBRow : resourceClassSetMap.entrySet()) {
        ResourcePair resourcePair = ResourcePair.of(resourceARow.getKey(), resourceBRow.getKey());
        Set<Resource> lcaSet = computeLCA(treeNodeMap, resourceARow.getValue(),
            resourceBRow.getValue());
        subsumerMap.put(simKey(resourcePair),
            lcaSet.stream().map(Resource::getId).collect(Collectors.toSet()));
      }
    }
    mapDB.commit();
  }

  @Override
  public Set<Resource> getLeastCommonSubsumersFor(ResourcePair resourcePair) {
    Set<String> resourceSet = subsumerMap.get(simKey(resourcePair));
    if (resourceSet != null) {
      return resourceSet.stream().map(Resource::new).collect(Collectors.toSet());
    } else {
      return Sets.newHashSet();
    }
  }

  /**
   * A naive implementation get a tree to solve the LCA problem.
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