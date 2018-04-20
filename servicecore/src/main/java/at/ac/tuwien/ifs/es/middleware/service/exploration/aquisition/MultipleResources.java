package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition.MultipleResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.sparql.SPARQLService;
import java.util.Collection;
import java.util.List;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionSource} that starts from a list of specified
 * resources. These resources must be specified as list of IRI strings of them for the {@code param}
 * argument of the {@link at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep}.
 * This operator will be registered at {@link at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}
 * as {@code esm.source.multiple}.
 * <p/>
 * It is expected that the given resource IRIs are valid and the corresponding resources exist in
 * the dataset.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.source.multiple")
public class MultipleResources implements AcquisitionSource<MultipleResourcesPayload> {

  private static final Logger logger = LoggerFactory.getLogger(MultipleResources.class);

  private static final String SELECT_NOT_EXIST_QUERY = "SELECT ?s WHERE {\n"
      + "    VALUES ?s {\n"
      + "      %s \n"
      + "    }\n"
      + "    FILTER NOT EXISTS {\n"
      + "    \t{?s ?p ?o .} UNION {?t ?s ?o} UNION {?t ?p ?s}\n"
      + "\t}\n"
      + "}";

  private SPARQLService sparqlService;

  public MultipleResources(@Autowired SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public Class<MultipleResourcesPayload> getParameterClass() {
    return MultipleResourcesPayload.class;
  }

  /*@Override
  public ExplorationContext apply(JsonNode resourceArray) {
    if (resourceArray.isArray()) {
      List<BlankNodeOrIRI> resources = new LinkedList<>();
      ArrayNode resourcesList = (ArrayNode) resourceArray;
      for (JsonNode resourceNode : resourcesList) {
        if (resourceNode.isValueNode()) {
          String resourceIRI = resourceNode.asText();
          try {
            resources.add(BlankOrIRIJsonUtil.valueOf(resourceIRI));
          } catch (IllegalArgumentException i) {
            throw new ExplorationFlowSpecificationException(
                String.format("The given resource with IRI '%s' is not valid.", resourceIRI));
          }
        } else {
          throw new ExplorationFlowSpecificationException(
              "The resources in the list must be specified as IRI string.");
        }
      }

    } else {
      throw new ExplorationFlowSpecificationException(
          "The resources must be specified as a list of corresponding IRI strings.");
    }
  }*/

  @Override
  public ExplorationContext apply(MultipleResourcesPayload payload) {
    List<BlankNodeOrIRI> resources = payload.getResources();
    if (!resources.isEmpty()) {
      logger.debug("A list of resources with IRIs {} was passed as source.", resources);
      SelectQueryResult notExistResult = (SelectQueryResult) sparqlService
          .query(String.format(SELECT_NOT_EXIST_QUERY,
              resources.stream().map(BlankOrIRIJsonUtil::stringForSPARQLResourceOf)
                  .reduce("", (a, b) -> a + "\n" + b)), true);
      Collection<RDFTerm> notExistingResources = notExistResult.value().column("s").values();
      if (notExistingResources.isEmpty()) {
        return new ResourceList(resources);
      } else {
        throw new ExplorationFlowSpecificationException(String.format(
            "These specified resources %s do not exist.", notExistingResources));
      }
    } else {
      throw new ExplorationFlowSpecificationException(
          "There must be at least one resource specified in the given list.");
    }
  }
}