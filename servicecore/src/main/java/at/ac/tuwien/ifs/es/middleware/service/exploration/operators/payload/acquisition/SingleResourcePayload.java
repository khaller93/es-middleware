package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.ExplorationFlowStepPayload;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * This is the parameter payload for {@link at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition.SingleResource}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class SingleResourcePayload implements ExplorationFlowStepPayload {

  private Resource resource;

  /**
   * Creates a new {@link SingleResourcePayload} considering the given {@code resource}.
   *
   * @param resource {@link Resource} that shall be considered and must not be null.
   */
  @JsonCreator
  public SingleResourcePayload(
      @JsonProperty(value = "resource", required = true) Resource resource) {
    checkArgument(resource != null,
        "The given resource must not be null.");
    this.resource = resource;
  }

  public Resource getResource() {
    return resource;
  }

  @Override
  public String toString() {
    return "SingleResourcePayload{" +
        "resource=" + resource +
        '}';
  }
}
