package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.tree.TreeNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * This class is a concrete implementation of {@link ClassHierarchyService} that uses {@link
 * SPARQLService} to gather the class hierarchy.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@RegisterForAnalyticalProcessing(name = ClassHierarchyWithSPARQLService.UID, requiresSPARQL = true,
    prerequisites = {SameAsResourceService.class})
public class ClassHierarchyWithSPARQLService implements ClassHierarchyService {

  private static final Logger logger = LoggerFactory
      .getLogger(ClassHierarchyWithSPARQLService.class);

  public static final String UID = "esm.service.analytics.dataset.class.hierarchy";

  private static final Integer LOAD_SIZE = 10000;

  private static final String ALL_SUBCLASSES_QUERY = "SELECT DISTINCT ?class ?superClass WHERE {\n"
      + "    {\n"
      + "      [] a ?class\n"
      + "    } UNION {\n"
      + "      ?class a rdfs:Class\n"
      + "    } UNION {\n"
      + "      ?class rdfs:subClassOf []\n"
      + "    } UNION {\n"
      + "      [] rdfs:subClassOf ?class\n"
      + "    }\n"
      + "    ?class rdfs:subClassOf+ ?superClass .\n"
      + "    FILTER (?class != ?superClass) .\n"
      + "    FILTER NOT EXISTS {\n"
      + "        ?class rdfs:subClassOf+ ?anotherSuperClass .\n"
      + "        ?anotherSuperClass rdfs:subClassOf+ ?superClass .\n"
      + "        FILTER (isIRI(?anotherClass)) .\n"
      + "    }\n"
      + "    FILTER(isIRI(?class) && isIRI(?superClass)) .\n"
      + "}\n"
      + "OFFSET %d\n"
      + "LIMIT %d";

  String test = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
      +"PREFIX vin: <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#>\n"
      +"select * where {\n"
      +"    VALUES ?class {\n"
      +"    \tvin:RedWine\n"
      +"    }\n"
      +"    OPTIONAL {\n"
      +"    \t?class rdfs:subClassOf+ ?superClass .\n"
      +"        FILTER NOT EXISTS {\n"
      +"            ?class rdfs:subClassOf+ ?anotherSuperClass .\n"
      +"            ?anotherSuperClass rdfs:subClassOf+ ?superClass .\n"
      +"            FILTER (isIRI(?anotherClass)) .\n"
      +"        }\n"
      +"      \tFILTER(isIRI(?superClass) && ?class != ?superClass) .\n"
      +"    }\n"
      +"}\n";

  private final SPARQLService sparqlService;
  private final SameAsResourceService sameAsResourceService;
  private final DB mapDB;

  private final HTreeMap<String, Integer> classNodeMap;
  private final HTreeMap<Integer, ClassTreeNode> treeNodeMap;

  private int treeNodeId;

  @Autowired
  public ClassHierarchyWithSPARQLService(
      SPARQLService sparqlService,
      SameAsResourceService sameAsResourceService, @Qualifier("persistent-mapdb") DB mapDB) {
    this.sparqlService = sparqlService;
    this.sameAsResourceService = sameAsResourceService;
    this.mapDB = mapDB;
    this.classNodeMap = mapDB
        .hashMap(UID + ".class.to.node.map", Serializer.STRING, Serializer.INTEGER).createOrOpen();
    this.treeNodeMap = mapDB.hashMap(UID + ".node.map", Serializer.INTEGER, Serializer.JAVA)
        .createOrOpen();
    this.treeNodeId = treeNodeMap.size();
  }

  private int getTreeNodeId() {
    return ++this.treeNodeId;
  }

  @Override
  public Set<Resource> getMostSpecificClasses(Set<Resource> classes) {
    checkArgument(classes != null, "The given list of classes must not be null.");
    Set<Resource> parents = classes.stream().flatMap(c -> getParentClasses(c).stream())
        .collect(Collectors.toSet());
    return classes.stream().filter(c -> !parents.contains(c)).collect(Collectors.toSet());
  }

  @Override
  public Set<Resource> getAllClasses(Set<Resource> classes) {
    checkArgument(classes != null, "The given list of classes must not be null.");
    Set<Resource> allClasses = new HashSet<>(classes);
    for (Resource resource : classes) {
      allClasses.addAll(getParentClasses(resource));
    }
    return allClasses;
  }

  @Override
  public Set<Resource> getParentClasses(Resource classResource) {
    checkArgument(classResource != null, "The given list of class resource must not be null.");
    return getTreeNodeFor(classResource)
        .map(classTreeNode -> getParentTreeNodes(classTreeNode).stream()
            .flatMap(tn -> tn.getResources().stream())
            .map(Resource::new).collect(Collectors.toSet())).orElseGet(HashSet::new);
  }

  private Set<ClassTreeNode> getParentTreeNodes(ClassTreeNode classTreeNode) {
    Set<ClassTreeNode> parentsList = new HashSet<>();
    for (ClassTreeNode treeNode : classTreeNode.getParents().stream().map(treeNodeMap::get)
        .collect(Collectors.toSet())) {
      parentsList.add(treeNode);
      parentsList.addAll(getParentTreeNodes(treeNode));
    }
    return parentsList;
  }

  @Override
  public Set<Resource> getLowestCommonAncestor(Resource classA, Resource classB) {
    checkArgument(classA != null && classB != null,
        "The given (classA, classB) pair must not be null.");
    Set<Resource> aParentClasses = getParentClasses(classA);
    if (aParentClasses.isEmpty()) {
      return Sets.newHashSet();
    }
    Set<Resource> bParentClasses = Sets.newHashSet(classB);
    do {
      Set<Resource> intersectionClasses = Sets.intersection(aParentClasses, bParentClasses)
          .immutableCopy();
      if (!intersectionClasses.isEmpty()) {
        return intersectionClasses;
      }
      bParentClasses = getNextLevelParent(new HashSet<>(bParentClasses));
    } while (!bParentClasses.isEmpty());
    return Sets.newHashSet();
  }

  public Set<Resource> getNextLevelParent(Set<Resource> resourceSet) {
    Set<Resource> nextLevelResourceSet = new HashSet<>();
    for (Resource resource : resourceSet) {
      Optional<ClassTreeNode> optTreeNode = getTreeNodeFor(resource);
      optTreeNode.ifPresent(classTreeNode -> nextLevelResourceSet.addAll(
          classTreeNode.getParents().stream().map(treeNodeMap::get).filter(Objects::nonNull)
              .flatMap(tn -> tn.getResources().stream()).map(Resource::new)
              .collect(Collectors.toSet())));
    }
    return nextLevelResourceSet;
  }

  @Override
  public void compute() {
    logger.info("Starting to build class hierarchy.");
    int offset = 0;
    List<Map<String, RDFTerm>> resultList;
    do {
      resultList = sparqlService.<SelectQueryResult>query(
          String.format(ALL_SUBCLASSES_QUERY, offset, LOAD_SIZE), true).value();
      for (Map<String, RDFTerm> row : resultList) {
        /* get or create tree node for class */
        Resource classResource = new Resource((BlankNodeOrIRI) row.get("class"));
        ClassTreeNode classTreenode = generateTreeNodeFor(classResource);
        /* get or create tree node for super class */
        Resource superClassResource = new Resource((BlankNodeOrIRI) row.get("superClass"));
        ClassTreeNode superClassTreenode = generateTreeNodeFor(superClassResource);
        /* create links */
        classTreenode.addParent(superClassTreenode.getId());
        superClassTreenode.addChildren(classTreenode.getId());
        treeNodeMap.put(classTreenode.getId(), classTreenode);
        treeNodeMap.put(superClassTreenode.getId(), superClassTreenode);
      }
      logger.debug("Loaded {} class <-> superclass links.", offset);
      offset += resultList.size();
    } while (resultList.size() == LOAD_SIZE);
    /* repair hierarchy */
    postProcessHierarchy();
    this.mapDB.commit();
  }

  private void postProcessHierarchy() {
    for (ClassTreeNode ctn : treeNodeMap.values()) {
      for (Integer childId : ctn.getChildren()) {
        ClassTreeNode childTreeNode = treeNodeMap.get(childId);
        if (childTreeNode != null) {
          childTreeNode.removeParent(ctn.getParents());
          treeNodeMap.put(childTreeNode.getId(), childTreeNode);
        }
      }
    }
    for (ClassTreeNode ctn : treeNodeMap.values()) {
      for (Integer parentId : ctn.getParents()) {
        ClassTreeNode parentNode = treeNodeMap.get(parentId);
        if (parentNode != null) {
          parentNode.removeChildren(ctn.getChildren());
          treeNodeMap.put(parentNode.getId(), parentNode);
        }
      }
    }
  }

  private ClassTreeNode generateTreeNodeFor(Resource classResource) {
    if (!classNodeMap.containsKey(classResource.getId())) {
      // create list of resources represented by the treenode.
      Set<Resource> resourcesList = sameAsResourceService.getSameAsResourcesFor(classResource);
      resourcesList.add(classResource);
      // store tree node.
      int treeNodeId = getTreeNodeId();
      ClassTreeNode classTreenode = new ClassTreeNode(treeNodeId,
          resourcesList.stream().map(Resource::getId).collect(
              Collectors.toSet()));
      treeNodeMap.put(treeNodeId, classTreenode);
      // link resources to tree node.
      for (Resource resource : resourcesList) {
        classNodeMap.put(resource.getId(), treeNodeId);
      }
      return classTreenode;
    } else {
      return treeNodeMap.get(classNodeMap.get(classResource.getId()));
    }
  }

  private Optional<ClassTreeNode> getTreeNodeFor(Resource resource) {
    Integer resourceTreenodeId = classNodeMap.get(resource.getId());
    if (resourceTreenodeId != null) {
      ClassTreeNode resourceTreenode = treeNodeMap.get(resourceTreenodeId);
      if (resourceTreenode != null) {
        return Optional.of(resourceTreenode);
      }
    }
    return Optional.empty();
  }

}
