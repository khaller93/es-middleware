package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
@RegisterForExplorationFlow("esm.source.multiple")
public class MultipleResources implements AcquisitionSource {

  @Override
  public Class<?> getParameterClass() {
    return null;
  }

  @Override
  public ExplorationContext apply(JsonNode parameterMap) {
    return null;
  }
}
