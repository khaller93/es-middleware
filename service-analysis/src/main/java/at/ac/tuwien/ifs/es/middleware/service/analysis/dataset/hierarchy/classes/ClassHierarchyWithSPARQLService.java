package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.serializer.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
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
    prerequisites = {AllClassesService.class, SameAsResourceService.class})
public class ClassHierarchyWithSPARQLService implements ClassHierarchyService {

  private static final Logger logger = LoggerFactory
      .getLogger(ClassHierarchyWithSPARQLService.class);

  public static final String UID = "esm.service.analytics.dataset.class.hierarchy";

  private static final Integer LOAD_SIZE = 10000;

  private static final String ALL_SUBCLASSES_QUERY =
      "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
          + "select ?class ?superClass where {\n"
          + "    VALUES ?class {\n"
          + "        %s\n"
          + "    }\n"
          + "    OPTIONAL {\n"
          + "        ?class rdfs:subClassOf+ ?superClass .\n"
          + "        FILTER(isIRI(?superClass) && ?class != ?superClass) .\n"
          + "    }\n"
          + "}\n";

  private final SPARQLService sparqlService;
  private final AllClassesService allClassesService;
  private final SameAsResourceService sameAsResourceService;
  private final Cache cache;
  private final DB mapDB;

  private final HTreeMap<String, Integer> classNodeMap;
  private final HTreeMap<Integer, ClassTreeNode> treeNodeMap;

  private int treeNodeId;

  @Autowired
  public ClassHierarchyWithSPARQLService(
      SPARQLService sparqlService,
      AllClassesService allClassesService,
      CacheManager cacheManager,
      SameAsResourceService sameAsResourceService, DB mapDB) {
    this.sparqlService = sparqlService;
    this.allClassesService = allClassesService;
    this.sameAsResourceService = sameAsResourceService;
    this.cache = cacheManager.getCache("class-hierarchy-cache");
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
    CKey<RLK> msKey = new CKey<>("lca-ms", new RLK(classes));
    ValueWrapper value = cache.get(msKey);
    if (value != null) {
      return (Set<Resource>) value.get();
    }
    Set<Resource> parents = classes.stream().flatMap(c -> getSuperClasses(c).stream())
        .collect(Collectors.toSet());
    Set<Resource> mostSpecificClasses = classes.stream().filter(c -> !parents.contains(c))
        .collect(Collectors.toSet());
    cache.put(msKey, mostSpecificClasses);
    return mostSpecificClasses;
  }

  @Override
  public Set<Resource> getAllClasses(Set<Resource> classes) {
    checkArgument(classes != null, "The given list of classes must not be null.");
    Set<Resource> allClasses = new HashSet<>(classes);
    for (Resource resource : classes) {
      allClasses.addAll(getSuperClasses(resource));
    }
    return allClasses;
  }

  @Override
  public Set<Resource> getSuperClasses(Resource classResource) {
    checkArgument(classResource != null, "The given class resource must not be null.");
    return getTreeNodeFor(classResource)
        .map(classTreeNode -> getParentTreeNodes(Sets.newHashSet(classTreeNode), classTreeNode)
            .stream()
            .flatMap(tn -> tn.getResources().stream())
            .map(Resource::new).collect(Collectors.toSet())).orElseGet(HashSet::new);
  }

  @Override
  public Set<Resource> getSubClasses(Resource classResource) {
    checkArgument(classResource != null, "The given class resource must not be null.");
    return getTreeNodeFor(classResource)
        .map(classTreeNode -> getChildrenTreeNodes(Sets.newHashSet(classTreeNode), classTreeNode)
            .stream()
            .flatMap(tn -> tn.getResources().stream())
            .map(Resource::new).collect(Collectors.toSet())).orElseGet(HashSet::new);
  }

  private Set<ClassTreeNode> getParentTreeNodes(Set<ClassTreeNode> visitedNodes,
      ClassTreeNode classTreeNode) {
    Set<ClassTreeNode> parentsList = new HashSet<>();
    for (ClassTreeNode treeNode : classTreeNode.getParents().stream().map(treeNodeMap::get)
        .collect(Collectors.toSet())) {
      parentsList.add(treeNode);
      if (!visitedNodes.contains(treeNode)) {
        Set<ClassTreeNode> newVisitedNodes = new HashSet<>(visitedNodes);
        newVisitedNodes.add(treeNode);
        parentsList.addAll(getParentTreeNodes(newVisitedNodes, treeNode));
      }
    }
    return parentsList;
  }

  private Set<ClassTreeNode> getChildrenTreeNodes(Set<ClassTreeNode> visitedNodes,
      ClassTreeNode classTreeNode) {
    Set<ClassTreeNode> childrenList = new HashSet<>();
    for (ClassTreeNode treeNode : classTreeNode.getChildren().stream().map(treeNodeMap::get)
        .collect(Collectors.toSet())) {
      childrenList.add(treeNode);
      if (!visitedNodes.contains(treeNode)) {
        Set<ClassTreeNode> newVisitedNodes = new HashSet<>(visitedNodes);
        newVisitedNodes.add(treeNode);
        childrenList.addAll(getChildrenTreeNodes(newVisitedNodes, treeNode));
      }
    }
    return childrenList;
  }

  @Override
  public Set<Resource> getLowestCommonAncestor(Resource classA, Resource classB) {
    checkArgument(classA != null && classB != null,
        "The given (classA, classB) pair must not be null.");
    CKey<Pair<Resource, Resource>> lcaKeyAB = new CKey<>("lca-single", Pair.of(classA, classB));
    CKey<Pair<Resource, Resource>> lcaKeyBA = new CKey<>("lca-single", Pair.of(classB, classA));
    ValueWrapper value = cache.get(lcaKeyAB);
    if (value != null) {
      return (Set<Resource>) value.get();
    }
    value = cache.get(lcaKeyBA);
    if (value != null) {
      return (Set<Resource>) value.get();
    }
    Set<Resource> aParentClasses = ImmutableSet.<Resource>builder()
        .addAll(getSuperClasses(classA)).add(classA).build();
    Set<Resource> bParentClasses = Sets.newHashSet(classB);
    do {
      Set<Resource> intersectionClasses = Sets.intersection(aParentClasses, bParentClasses)
          .immutableCopy();
      if (!intersectionClasses.isEmpty()) {
        cache.put(lcaKeyAB, intersectionClasses);
        return intersectionClasses;
      }
      bParentClasses = getNextLevelParent(new HashSet<>(bParentClasses));
    } while (!bParentClasses.isEmpty());
    cache.put(lcaKeyAB, Sets.newHashSet());
    return Sets.newHashSet();
  }

  @Override
  public Set<Resource> getLowestCommonAncestor(Set<Resource> classASet, Set<Resource> classBSet) {
    checkArgument(classASet != null && classBSet != null,
        "Both given sets of classes must not be null.");
    if (classASet.isEmpty() || classBSet.isEmpty()) {
      return Sets.newHashSet();
    }
    // check the cache
    RLK rlkA = new RLK(classASet);
    RLK rlkB = new RLK(classBSet);
    CKey<Pair<RLK, RLK>> cKeyAB = new CKey<>("lca-sets", Pair.of(rlkA, rlkB));
    CKey<Pair<RLK, RLK>> cKeyBA = new CKey<>("lca-sets", Pair.of(rlkB, rlkA));
    ValueWrapper value = cache.get(cKeyAB);
    if (value != null) {
      return (Set<Resource>) value.get();
    }
    value = cache.get(cKeyBA);
    if (value != null) {
      return (Set<Resource>) value.get();
    }
    // perform the function
    Set<Resource> mostSpecificClassesA = getMostSpecificClasses(classASet);
    Set<Resource> mostSpecificClassesB = getMostSpecificClasses(classBSet);
    if (mostSpecificClassesA.size() == 1 && mostSpecificClassesB.size() == 1) {
      Set<Resource> lowestCommonAncestor = getLowestCommonAncestor(
          mostSpecificClassesA.iterator().next(),
          mostSpecificClassesB.iterator().next());
      cache.put(cKeyAB, lowestCommonAncestor);
      return lowestCommonAncestor;
    } else {
      Set<Resource> lcaCandidates = new HashSet<>();
      for (Resource mscA : mostSpecificClassesA) {
        for (Resource mscB : mostSpecificClassesA) {
          lcaCandidates.addAll(getLowestCommonAncestor(mscA, mscB));
        }
      }
      Set<Resource> lowestCommonAncestor = getMostSpecificClasses(lcaCandidates);
      cache.put(cKeyAB, lowestCommonAncestor);
      return lowestCommonAncestor;
    }
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
    List<Resource> classList = new LinkedList<>(allClassesService.getAllClasses());
    for (int i = 0; i < classList.size(); i += LOAD_SIZE) {
      List<Map<String, RDFTerm>> resultList = sparqlService.<SelectQueryResult>query(
          String.format(ALL_SUBCLASSES_QUERY, classList.subList(i,
              (i + LOAD_SIZE) <= classList.size() ? (i + LOAD_SIZE) : classList.size()).stream()
              .map(RDFTermJsonUtil::stringForSPARQLResourceOf)
              .collect(Collectors.joining("\n"))), true)
          .value();
      for (Map<String, RDFTerm> row : resultList) {
        /* get or create tree node for class */
        Resource classResource = new Resource((BlankNodeOrIRI) row.get("class"));
        ClassTreeNode classTreenode = generateTreeNodeFor(classResource);
        /* get or create tree node for super class */
        BlankNodeOrIRI superClassBOrIRI = (BlankNodeOrIRI) row.get("superClass");
        if (superClassBOrIRI != null) {
          Resource superClassResource = new Resource((BlankNodeOrIRI) row.get("superClass"));
          ClassTreeNode superClassTreenode = generateTreeNodeFor(superClassResource);
          /* create links */
          classTreenode.addParent(superClassTreenode.getId());
          superClassTreenode.addChildren(classTreenode.getId());
          treeNodeMap.put(classTreenode.getId(), classTreenode);
          treeNodeMap.put(superClassTreenode.getId(), superClassTreenode);
        }
      }
      logger.debug("Loaded {} class <-> superclass links.", i + resultList.size());
    }
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

  static class CKey<T extends Serializable> implements Serializable {

    private String name;
    private T secondKey;

    public CKey(String name, T secondKey) {
      checkNotNull(name);
      checkNotNull(secondKey);
      this.name = name;
      this.secondKey = secondKey;
    }

    public String getName() {
      return name;
    }

    public T getSecondKey() {
      return secondKey;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CKey<?> cKey = (CKey<?>) o;
      return name.equals(cKey.name) &&
          secondKey.equals(cKey.secondKey);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, secondKey);
    }
  }

  static class RLK implements Serializable {

    private final List<Resource> resourceList;

    RLK(Set<Resource> resourceList) {
      checkNotNull(resourceList);
      this.resourceList = resourceList.stream().sorted(
          Comparator.comparing(Resource::getId)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      RLK that = (RLK) o;
      if (!(resourceList.size() == that.resourceList.size())) {
        return false;
      }
      for (int i = 0; i < resourceList.size(); i++) {
        if (!resourceList.get(i).equals(that.resourceList.get(i))) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      return Objects
          .hash(resourceList.stream().map(Resource::getId).collect(Collectors.joining(",")));
    }

    @Override
    public String toString() {
      return "RLK[" + resourceList.stream().map(Resource::getId)
          .collect(Collectors.toList()) + ']';
    }

  }
}
