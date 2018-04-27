package at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.VoidPayload;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.sparql.SPARQLService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an {@link AggregationOperator} that eliminates duplicates in an {@link
 * at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList}. Duplicates are resources
 * which hold an {@code owl:sameAs} relationship.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.aggregate.distinct")
public class Distinct implements AggregationOperator<VoidPayload> {

  private SPARQLService sparqlService;

  public Distinct(@Autowired SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public Class<VoidPayload> getParameterClass() {
    return VoidPayload.class;
  }

  private static final String DUPLICATES_QUERY = "SELECT ?s1 ?s2 WHERE { \n"
      + "VALUES ?s1 {\n"
      + "  ${resourceList}\n"
      + "}\n"
      + "VALUES ?s2 {\n"
      + "  ${resourceList}\n"
      + "}\n"
      + "?s1 owl:sameAs ?s2 .\n"
      + "FILTER(?s1 != ?s2) ."
      + "}";

  @Override
  public ExplorationContext apply(ExplorationContext context, VoidPayload payload) {
    if (context instanceof ResourceList) {
      ResourceList resourceList = (ResourceList) context;
      SelectQueryResult queryResult = (SelectQueryResult) sparqlService.query(new StrSubstitutor(
          Collections.singletonMap("resourceList", resourceList.asResourceSet().stream().map(
              BlankOrIRIJsonUtil::stringForSPARQLResourceOf).collect(Collectors.joining("\n"))))
          .replace(DUPLICATES_QUERY), true);
      Map<Resource, List<Resource>> duplicatesMaps = new HashMap<>();
      for (Map<String, RDFTerm> row : queryResult.value()) {
        List<Resource> duplicates = duplicatesMaps
            .compute(new Resource((BlankNodeOrIRI) row.get("s1")), (resource, resources) ->
                resources == null ? new LinkedList<>() : resources);
        duplicates.add(new Resource((BlankNodeOrIRI) row.get("s2")));
      }
      List<Resource> newResourceList = new LinkedList<>();
      Set<Resource> recognizedResources = new HashSet<>();
      for (Resource resource : resourceList) {
        if (!recognizedResources.contains(resource)) {
          newResourceList.add(resource);
          recognizedResources.add(resource);
          recognizedResources
              .addAll(duplicatesMaps.getOrDefault(resource, Collections.emptyList()));
        }
      }
      return newResourceList.stream().collect(resourceList);
    } else {
      throw new ExplorationFlowSpecificationException(
          "The given exploration flow is not valid, because a distinct operator needs a resource list context.");
    }
  }
}
