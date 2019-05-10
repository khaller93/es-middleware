package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IterableResourcesContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.Neighbourhood;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceCollection;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.exploitation.ExploitationOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.NeighbourhoodOpPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This operator generates the neighbourhood of {@link Resource}s that are passed on by the result
 * of the previous result. Thus, the previous step must return a {@link IterableResourcesContext}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.source.neighbourhood")
public class NeighbourhoodOperator implements
    ExploitationOperator<ResourceCollection, Neighbourhood, NeighbourhoodOpPayload> {

  private static final String QUERY = "SELECT ?s ?p ?o WHERE { \n"
      + "VALUES ?s {\n"
      + "  ${resourceList}\n"
      + "}\n"
      + "${propertyInclusion}\n"
      + "?s ?p ?o .\n"
      + "${propertyExclusion}\n"
      + "FILTER(isIRI(?o)) .\n"
      + "}";

  private static final String PROP_VALUES = "VALUES ?p {\n"
      + "${propertyList}\n"
      + "}";

  private static final String MINUS_PROP_VALUES = "MINUS  {\n"
      + "VALUES ?pm {\n"
      + "${propertyList}\n"
      + "}\n"
      + "?s ?pm ?o\n"
      + "}";

  private final SPARQLService sparqlService;

  @Autowired
  public NeighbourhoodOperator(SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public String getUID() {
    return "esm.source.neighbourhood";
  }

  @Override
  public Class<ResourceCollection> getExplorationContextInputClass() {
    return ResourceCollection.class;
  }

  @Override
  public Class<Neighbourhood> getExplorationContextOutputClass() {
    return Neighbourhood.class;
  }

  @Override
  public Class<NeighbourhoodOpPayload> getPayloadClass() {
    return NeighbourhoodOpPayload.class;
  }

  @Override
  public Neighbourhood apply(ResourceCollection source, NeighbourhoodOpPayload payload) {
    /* construct query */
    Map<String, String> valueMap = new HashMap<>();
    valueMap.put("resourceList", source.asResourceSet().stream().map(
        BlankOrIRIJsonUtil::stringForSPARQLResourceOf).collect(Collectors.joining("\n")));
    // deal with included properties
    List<Resource> includedProperties = payload.getIncludedProperties();
    if (includedProperties != null && !includedProperties.isEmpty()) {
      valueMap.put("propertyInclusion", new StringSubstitutor(
          Collections.singletonMap("propertyList", includedProperties.stream().map(
              BlankOrIRIJsonUtil::stringForSPARQLResourceOf).collect(Collectors.joining("\n"))))
          .replace(PROP_VALUES));
    } else {
      valueMap.put("propertyInclusion", "");
    }
    // deal with excluded properties
    List<Resource> excludedProperties = payload.getExcludedProperties();
    if (excludedProperties != null && !excludedProperties.isEmpty()) {
      valueMap.put("propertyExclusion", new StringSubstitutor(
          Collections.singletonMap("propertyList", excludedProperties.stream().map(
              BlankOrIRIJsonUtil::stringForSPARQLResourceOf).collect(Collectors.joining("\n"))))
          .replace(MINUS_PROP_VALUES));
    } else {
      valueMap.put("propertyExclusion", "");
    }
    /* query and unpack results */
    List<Map<String, RDFTerm>> valueSet = sparqlService.<SelectQueryResult>query(
        new StringSubstitutor(valueMap).replace(QUERY), true)
        .value();
    Map<Resource, Map<Resource, List<Resource>>> nMap = new HashMap<>();
    for (Map<String, RDFTerm> row : valueSet) {
      Resource subject = new Resource((BlankNodeOrIRI) row.get("s"));
      Resource property = new Resource((BlankNodeOrIRI) row.get("p"));
      Resource object = new Resource((BlankNodeOrIRI) row.get("o"));
      nMap.compute(subject,
          (resource, resourceListMap) -> {
            Map<Resource, List<Resource>> propMap =
                resourceListMap != null ? resourceListMap : new HashMap<>();
            propMap.compute(property,
                (resource1, list) -> {
                  List<Resource> objectList = list != null ? list : new LinkedList<>();
                  objectList.add(object);
                  return objectList;
                });
            return propMap;
          });
    }
    Neighbourhood neighbourhood = Neighbourhood.of(nMap);
    neighbourhood.mergeValues(source.getAllValues());
    return neighbourhood;
  }
}
