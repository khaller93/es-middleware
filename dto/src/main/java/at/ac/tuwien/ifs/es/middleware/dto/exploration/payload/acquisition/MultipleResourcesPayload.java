package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * This is the parameter payload for multiple resource operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class MultipleResourcesPayload implements Serializable {

  @JsonSerialize(contentUsing = BlankOrIRIJsonUtil.Serializer.class)
  @JsonDeserialize(contentUsing = BlankOrIRIJsonUtil.Deserializer.class)
  private List<BlankNodeOrIRI> resources;

  public MultipleResourcesPayload(
      @JsonProperty(value = "resources", required = true) List<BlankNodeOrIRI> resources) {
    this.resources = resources;
  }

  public List<BlankNodeOrIRI> getResources() {
    return resources;
  }

  @Override
  public String toString() {
    return "MultipleResourcesPayload{" +
        "resources=" + resources +
        '}';
  }
}
