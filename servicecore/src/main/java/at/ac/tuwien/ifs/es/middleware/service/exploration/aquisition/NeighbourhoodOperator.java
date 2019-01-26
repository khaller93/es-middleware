package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IterableResourcesContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.Neighbourhood;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.payload.acquisition.NeighbourhoodOpPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class NeighbourhoodOperator implements AcquisitionOperator<NeighbourhoodOpPayload> {

  private static final Logger logger = LoggerFactory.getLogger(NeighbourhoodOperator.class);

  private static final String QUERY = "SELECT ?s ?p ?o WHERE { \n"
      + "VALUES ?s {\n"
      + "  ${resourceList}\n"
      + "}\n"
      + "?s ?p ?o .\n"
      + "FILTER(isIRI(?o)) .\n"
      + "}";

  private final SPARQLService sparqlService;

  @Autowired
  public NeighbourhoodOperator(SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public Class<NeighbourhoodOpPayload> getParameterClass() {
    return NeighbourhoodOpPayload.class;
  }

  @Override
  public ExplorationContext apply(ExplorationContext context, NeighbourhoodOpPayload payload) {
    logger.debug("A neighbourhood computation of resources {} has been requested with {}.", payload,
        context);
    if (context instanceof IterableResourcesContext) {
      IterableResourcesContext source = (IterableResourcesContext) context;
      /* construct query */
      Map<String, String> valueMap = new HashMap<>();
      valueMap.put("resourceList", source.asResourceSet().stream().map(
          BlankOrIRIJsonUtil::stringForSPARQLResourceOf).collect(Collectors.joining("\n")));
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
      /*  */

      return neighbourhood;
    } else {
      throw new ExplorationFlowSpecificationException(String.format(
          "The result of the previous step must allow to iterate over resources, but for '%s' this is not the case.",
          context.getClass().getSimpleName()));
    }
  }
}
