package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.IterableResourcesContext;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.neighbourhood.Neighbourhood;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.RDFValueTerm;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources.ResourceCollection;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.sparql.result.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.exploitation.ExploitationOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.NeighbourhoodOpPayload;
import at.ac.tuwien.ifs.es.middleware.common.exploration.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.common.knowledgegraph.SPARQLService;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
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
@RegisterForExplorationFlow(NeighbourhoodOperator.OID)
public class NeighbourhoodOperator implements
    ExploitationOperator<ResourceCollection, Neighbourhood, NeighbourhoodOpPayload> {

  public static final String OID = "esm.source.neighbourhood";

  private static final String QUERY = "SELECT ?s ?p ?o WHERE { \n"
      + "VALUES ?s {\n"
      + "  ${resourceList}\n"
      + "}\n"
      + "${propertyInclusion}\n"
      + "?s ?p ?o .\n"
      + "${propertyExclusion}\n"
      + "FILTER(isIRI(?o) || isLiteral(?o)) .\n"
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
    return OID;
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
        RDFTermJsonUtil::stringForSPARQLResourceOf).collect(Collectors.joining("\n")));
    // deal with included properties
    List<Resource> includedProperties = payload.getIncludedProperties();
    if (includedProperties != null && !includedProperties.isEmpty()) {
      valueMap.put("propertyInclusion", new StringSubstitutor(
          Collections.singletonMap("propertyList", includedProperties.stream().map(
              RDFTermJsonUtil::stringForSPARQLResourceOf).collect(Collectors.joining("\n"))))
          .replace(PROP_VALUES));
    } else {
      valueMap.put("propertyInclusion", "");
    }
    // deal with excluded properties
    List<Resource> excludedProperties = payload.getExcludedProperties();
    if (excludedProperties != null && !excludedProperties.isEmpty()) {
      valueMap.put("propertyExclusion", new StringSubstitutor(
          Collections.singletonMap("propertyList", excludedProperties.stream().map(
              RDFTermJsonUtil::stringForSPARQLResourceOf).collect(Collectors.joining("\n"))))
          .replace(MINUS_PROP_VALUES));
    } else {
      valueMap.put("propertyExclusion", "");
    }
    /* query and unpack results */
    List<Map<String, org.apache.commons.rdf.api.RDFTerm>> valueSet = sparqlService.<SelectQueryResult>query(
        new StringSubstitutor(valueMap).replace(QUERY), true)
        .value();
    Map<Resource, Map<Resource, List<RDFValueTerm>>> nMap = new HashMap<>();
    for (Map<String, org.apache.commons.rdf.api.RDFTerm> row : valueSet) {
      Resource subject = new Resource((BlankNodeOrIRI) row.get("s"));
      Resource property = new Resource((BlankNodeOrIRI) row.get("p"));
      RDFValueTerm object = RDFValueTerm.of(row.get("o"));
      nMap.compute(subject,
          (resource, resourceListMap) -> {
            Map<Resource, List<RDFValueTerm>> propMap =
                resourceListMap != null ? resourceListMap : new HashMap<>();
            propMap.compute(property,
                (resource1, list) -> {
                  List<RDFValueTerm> objectList = list != null ? list : new LinkedList<>();
                  objectList.add(object);
                  return objectList;
                });
            return propMap;
          });
    }
    Neighbourhood neighbourhood = Neighbourhood.of(nMap);
    neighbourhood.values().merge(source.values());
    return neighbourhood;
  }
}
