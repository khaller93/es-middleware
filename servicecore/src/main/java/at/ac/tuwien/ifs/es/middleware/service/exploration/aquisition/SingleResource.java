package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.AskQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition.SingleResourcePayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.sparql.SPARQLService;
import java.util.Collections;
import java.util.Map;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionSource} that starts from a specified single
 * resource. This resource must be specified as simple IRI string as value to the {@code param}
 * argument of this {@link at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep}.
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
@RegisterForExplorationFlow("esm.source.single")
public class SingleResource implements AcquisitionSource<SingleResourcePayload> {

  private static final Logger logger = LoggerFactory.getLogger(SingleResource.class);

  private static final String ASK_EXIST_QUERY = "ASK WHERE { {${s} ?p ?o .} UNION {?t ${s} ?o} UNION {?t ?p ${s}} }";

  private SPARQLService sparqlService;

  public SingleResource(@Autowired SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public Class<SingleResourcePayload> getParameterClass() {
    return SingleResourcePayload.class;
  }


  @Override
  public ExplorationContext apply(SingleResourcePayload payload) {
    Map<String, String> valueMap = Collections
        .singletonMap("s",
            BlankOrIRIJsonUtil.stringForSPARQLResourceOf(payload.getResource().value()));
    AskQueryResult existResult = (AskQueryResult) sparqlService
        .query(new StrSubstitutor(valueMap).replace(ASK_EXIST_QUERY), true);
    if (existResult.value()) {
      return new ResourceList(Collections.singletonList(payload.getResource()));
    } else {
      throw new ExplorationFlowSpecificationException(
          String.format("The given IRI '%s' does not exist.", payload.getResource()));
    }
  }
}
