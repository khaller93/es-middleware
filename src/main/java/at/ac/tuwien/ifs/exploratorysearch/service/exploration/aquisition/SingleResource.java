package at.ac.tuwien.ifs.exploratorysearch.service.exploration.aquisition;

import at.ac.tuwien.ifs.exploratorysearch.dto.exploration.ExplorationResponse;
import at.ac.tuwien.ifs.exploratorysearch.dto.exploration.ResourceJsonUtil;
import at.ac.tuwien.ifs.exploratorysearch.dto.exploration.ResourceList;
import at.ac.tuwien.ifs.exploratorysearch.service.exception.ExplorationFlowSpecificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Collections;
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
@Component("SingleAcquisitionSource")
public class SingleResource implements AcquisitionSource {

  @Override
  public ExplorationResponse apply(JsonNode parameterMap) {
    if (parameterMap.has("iri")) {
      JsonNode singleResource = parameterMap.get("iri");
      if (singleResource.isValueNode()) {
        return new ExplorationResponse(
            new ResourceList(
                Collections.singletonList(ResourceJsonUtil.resourceOf(singleResource.asText()))),
            JsonNodeFactory.instance.objectNode());
      } else {
        throw new ExplorationFlowSpecificationException(
            "The specified single resource ('single') must be a simple string value");
      }
    } else {
      throw new ExplorationFlowSpecificationException(
          "The single resource 'single' must be specified for this source.");
    }
  }
}
