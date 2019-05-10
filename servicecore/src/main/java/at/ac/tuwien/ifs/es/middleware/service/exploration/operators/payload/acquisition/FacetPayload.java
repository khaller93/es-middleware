package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition;

import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.ExplorationFlowStepPayload;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import org.apache.commons.rdf.api.RDFTerm;

public final class FacetPayload implements ExplorationFlowStepPayload {

  private RDFTerm value;

  @JsonCreator
  public FacetPayload(@JsonProperty(value = "value", required = true) RDFTerm value) {
    this.value = value;
  }

  public RDFTerm getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "FacetPayload{" +
        "value=" + value +
        '}';
  }
}
