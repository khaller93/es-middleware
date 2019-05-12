package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.ResourceCollection;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.AskQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.SingleResourcePayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation get {@link AcquisitionSource} that starts from a specified single
 * resource. This resource must be specified as simple IRI string as value to the {@code param}
 * argument get this {@link at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep}.
 * This operator will be registered at {@link at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}
 * as {@code esm.source.single}.
 * <p/>
 * It is expected that the specified resource IRI is valid and such a resource exists in the
 * dataset.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow(SingleResource.OID)
public class SingleResource implements
    AcquisitionSource<ResourceCollection, SingleResourcePayload> {

  public static final String OID = "esm.source.single";

  private static final String ASK_EXIST_QUERY = "ASK WHERE { {${s} ?p ?o .} UNION {?t ${s} ?o} UNION {?t ?p ${s}} }";

  private final SPARQLService sparqlService;

  @Autowired
  public SingleResource(SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public String getUID() {
    return OID;
  }

  @Override
  public Class<ResourceCollection> getExplorationContextOutputClass() {
    return ResourceCollection.class;
  }

  @Override
  public Class<SingleResourcePayload> getPayloadClass() {
    return SingleResourcePayload.class;
  }


  @Override
  public ResourceCollection apply(SingleResourcePayload payload) {
    Map<String, String> valueMap = Collections
        .singletonMap("s",
            BlankOrIRIJsonUtil.stringForSPARQLResourceOf(payload.getResource().value()));
    AskQueryResult existResult = sparqlService
        .query(new StringSubstitutor(valueMap).replace(ASK_EXIST_QUERY), true);
    if (existResult.value()) {
      return new ResourceList(Collections.singletonList(payload.getResource()));
    } else {
      throw new ExplorationFlowSpecificationException(
          String.format("The given IRI '%s' does not exist.", payload.getResource()));
    }
  }
}
