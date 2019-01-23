package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.service.exploration.payload.acquisition.FacetPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
@RegisterForExplorationFlow("esm.source.facet")
public class Facets implements AcquisitionSource<FacetPayload> {

  private static final Logger logger = LoggerFactory.getLogger(Facets.class);

  @Override
  public Class<FacetPayload> getParameterClass() {
    return FacetPayload.class;
  }

  @Override
  public ExplorationContext apply(FacetPayload payload) {
    logger.info(">>>>> {}", payload);
    return new ResourceList();
  }
}
