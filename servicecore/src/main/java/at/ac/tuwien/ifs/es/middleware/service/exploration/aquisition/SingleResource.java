package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionSource} that starts from a specified single
 * resource.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.source.single")
public class SingleResource implements AcquisitionSource {

  private static final Logger logger = LoggerFactory.getLogger(SingleResource.class);

  private ObjectMapper objectMapper;

  public SingleResource(@Autowired ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Class<String> getParameterClass() {
    return String.class;
  }

  @Override
  public ExplorationContext apply(JsonNode parameterMap) {
    try {
      String iri = objectMapper.treeToValue(parameterMap, getParameterClass());
      logger.debug("A single resource with IRI '{}' was passed as source.", iri);
      //TODO: Implement
      return null;
    } catch (JsonProcessingException e) {
      throw new ExplorationFlowSpecificationException(
          "A single resource must be specified as simple string for this source.", e);
    }
  }

}
