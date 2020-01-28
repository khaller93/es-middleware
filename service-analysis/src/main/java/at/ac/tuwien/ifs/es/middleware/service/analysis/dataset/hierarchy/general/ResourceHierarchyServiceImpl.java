package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.general;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.serializer.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SPARQLService;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResourceHierarchyServiceImpl implements ResourceHierarchyService {

  private final Logger logger = LoggerFactory.getLogger(ResourceHierarchyServiceImpl.class);

  private final String BOTTOM_UP_QUERY = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
      + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
      + "\n"
      + "select DISTINCT ?class ?superClass where {\n"
      + "    %s\n"
      + "    OPTIONAL {\n"
      + "        ?class (%s)+ ?superClass.\n"
      + "        FILTER (?class != ?superClass).\n"
      + "    }\n"
      + "}";

  private final String TOP_DOWN_QUERY = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
      + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
      + "\n"
      + "select DISTINCT ?class ?subClass where {\n"
      + "    %s\n"
      + "    OPTIONAL {\n"
      + "        ?class (%s)+ ?subClass.\n"
      + "        FILTER (?class != ?subClass).\n"
      + "    }\n"
      + "}";

  private final SPARQLService sparqlService;

  @Autowired
  public ResourceHierarchyServiceImpl(
      SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public void compute() {

  }

  private void walk(List<ResourceNode> successors, List<ResourceNode> parents) {
    for (ResourceNode tn : successors) {
      LinkedList<ResourceNode> newParents = new LinkedList<>(parents);
      newParents.add(tn);

      Set<ResourceNode> childResources = tn.getChildResources();
      for (ResourceNode child : childResources) {
        child.removeAllParents(parents);
        parents.forEach(p -> p.removeChild(child));
        walk(Collections.singletonList(child), newParents);
      }
    }
  }

  private Optional<String> getBottomUpSPARQLQuery(Set<Resource> includeClasses,
      Set<Resource> excludeClasses,
      Set<Resource> bottomUpProperties) {
    if (bottomUpProperties != null && !bottomUpProperties.isEmpty()) {
      String source = "?s a owl:Thing.";
      if (includeClasses != null && !includeClasses.isEmpty()) {
        source = includeClasses.stream()
            .map(s -> String.format("{?s a %s}", RDFTermJsonUtil.stringForSPARQLResourceOf(s)))
            .collect(Collectors.joining(" UNION"));
      }
      if (excludeClasses != null && !excludeClasses.isEmpty()) {
        source += String.format("\n MINUS \n {%s}",
            excludeClasses.stream().map(s -> String.format("{?s a %s}", s))
                .collect(Collectors.joining(" UNION")));
      }
      return Optional.of(String.format(BOTTOM_UP_QUERY, source,
          bottomUpProperties.stream().map(RDFTermJsonUtil::stringForSPARQLResourceOf)
              .collect(Collectors.joining("|"))));
    }
    return Optional.empty();
  }

  private Optional<String> getTopDownSPARQLQuery(Set<Resource> includeClasses,
      Set<Resource> excludeClasses,
      Set<Resource> topDownProperties) {
    if (topDownProperties != null && !topDownProperties.isEmpty()) {
      String source = "?s a owl:Thing.";
      if (includeClasses != null && !includeClasses.isEmpty()) {
        source = includeClasses.stream()
            .map(s -> String.format("{?s a %s}", RDFTermJsonUtil.stringForSPARQLResourceOf(s)))
            .collect(Collectors.joining(" UNION"));
      }
      if (excludeClasses != null && !excludeClasses.isEmpty()) {
        source += String.format("\n MINUS \n {%s}",
            excludeClasses.stream().map(s -> String.format("{?s a %s}", s))
                .collect(Collectors.joining(" UNION")));
      }
      return Optional.of(String.format(TOP_DOWN_QUERY, source,
          topDownProperties.stream().map(RDFTermJsonUtil::stringForSPARQLResourceOf)
              .collect(Collectors.joining("|"))));
    }
    return Optional.empty();
  }

  @Override
  public List<ResourceNode> getHierarchy(Set<Resource> includeClasses, Set<Resource> excludeClasses,
      Set<Resource> topDownProperties, Set<Resource> bottomUpProperties) {

    Map<Resource, ResourceNode> hierarchyMap = new HashMap<>();

    Optional<String> bottomUpQuery = getBottomUpSPARQLQuery(includeClasses, excludeClasses,
        bottomUpProperties);
    if (bottomUpQuery.isPresent()) {
      List<Map<String, RDFTerm>> bottomUpValues = sparqlService.<SelectQueryResult>query(
          bottomUpQuery.get(), true).value();
      for (Map<String, RDFTerm> row : bottomUpValues) {
        Resource clazzResource = new Resource((BlankNodeOrIRI) row.get("class"));
        if (!hierarchyMap.containsKey(clazzResource)) {
          hierarchyMap.put(clazzResource, new ResourceNode(clazzResource));
        }
        RDFTerm superClassTerm = row.get("superClass");
        if (superClassTerm != null) {
          Resource superClazzResource = new Resource((BlankNodeOrIRI) superClassTerm);
          if (!hierarchyMap.containsKey(superClazzResource)) {
            hierarchyMap.put(superClazzResource, new ResourceNode(superClazzResource));
          }
          ResourceNode currentNode = hierarchyMap.get(clazzResource);
          ResourceNode parentNode = hierarchyMap.get(superClazzResource);
          currentNode.addParentResourceNode(parentNode);
          parentNode.addChildResourceNode(currentNode);
        }
      }
    }

    Optional<String> topDownQuery = getTopDownSPARQLQuery(includeClasses, excludeClasses,
        topDownProperties);
    if (topDownQuery.isPresent()) {
      List<Map<String, RDFTerm>> bottomUpValues = sparqlService.<SelectQueryResult>query(
          topDownQuery.get(), true).value();
      for (Map<String, RDFTerm> row : bottomUpValues) {
        Resource clazzResource = new Resource((BlankNodeOrIRI) row.get("class"));
        if (!hierarchyMap.containsKey(clazzResource)) {
          hierarchyMap.put(clazzResource, new ResourceNode(clazzResource));
        }
        RDFTerm superClassTerm = row.get("subClass");
        if (superClassTerm != null) {
          Resource superClazzResource = new Resource((BlankNodeOrIRI) superClassTerm);
          if (!hierarchyMap.containsKey(superClazzResource)) {
            hierarchyMap.put(superClazzResource, new ResourceNode(superClazzResource));
          }
          ResourceNode currentNode = hierarchyMap.get(clazzResource);
          ResourceNode childNode = hierarchyMap.get(superClazzResource);
          currentNode.addChildResourceNode(childNode);
          childNode.addParentResourceNode(currentNode);
        }
      }
    }

    walk(hierarchyMap.values().stream().filter(rn -> rn.getParentResources().isEmpty())
        .collect(Collectors.toList()), Collections.emptyList());

    return new LinkedList<>(hierarchyMap.values());
  }
}
