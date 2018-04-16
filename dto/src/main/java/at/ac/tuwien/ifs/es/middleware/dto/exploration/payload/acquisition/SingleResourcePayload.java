package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * This is the parameter payload for single resource operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class SingleResourcePayload implements Serializable {

  @JsonSerialize(using = BlankOrIRIJsonUtil.Serializer.class)
  @JsonDeserialize(using = BlankOrIRIJsonUtil.Deserializer.class)
  private BlankNodeOrIRI resource;

  public SingleResourcePayload(
      @JsonProperty(value = "resource", required = true) BlankNodeOrIRI resource) {
    this.resource = resource;
  }

  public BlankNodeOrIRI getResource() {
    return resource;
  }

  @Override
  public String toString() {
    return "SingleResourcePayload{" +
        "resource=" + resource +
        '}';
  }
}
