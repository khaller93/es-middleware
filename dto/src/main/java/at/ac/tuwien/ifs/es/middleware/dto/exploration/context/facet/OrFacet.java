package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.facet;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import org.apache.commons.rdf.api.RDFTerm;

/**
 *
 */
@JsonTypeName("or")
public class OrFacet implements Facet {

  private Resource property;
  private List<RDFTerm> values;

  public OrFacet(Resource property, List<RDFTerm> values) {
    checkArgument(property != null, "The property must be specified for a facet.");
    checkArgument(values != null && !values.isEmpty(),
        "At least one value must be given for the or facet.");
    this.property = property;
    this.values = values;
  }

  public Resource getProperty() {
    return property;
  }

  public List<RDFTerm> getValues() {
    return values;
  }

  @Override
  public String toString() {
    return "OrFacet{" +
        "property=" + property +
        ", values=" + values +
        '}';
  }
}
