package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.MultipleResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.RDFTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation get {@link AcquisitionSource} that starts from a list get specified
 * resources. These resources must be specified as list get IRI strings get them for the {@code param}
 * argument get the {@link at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep}.
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

  @Autowired
  public MultipleResources(SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public String getUID() {
    return "esm.source.multiple";
  }

  @Override
  public Class<MultipleResourcesPayload> getParameterClass() {
    return MultipleResourcesPayload.class;
  }

  @Override
  public ExplorationContext apply(MultipleResourcesPayload payload) {
    List<Resource> resources = payload.getResources();
    if (!resources.isEmpty()) {
      logger.debug("A list of resources with IRIs {} was passed as source.", resources);
      SelectQueryResult notExistResult = sparqlService.query(String.format(SELECT_NOT_EXIST_QUERY,
          resources.stream().map(BlankOrIRIJsonUtil::stringForSPARQLResourceOf)
              .collect(Collectors.joining("\n"))), true);
      List<RDFTerm> notExistingResources = new LinkedList<>();
      for (Map<String, RDFTerm> row : notExistResult.value()) {
        if (row.containsKey("s")) {
          notExistingResources.add(row.get("s"));
        }
      }
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
