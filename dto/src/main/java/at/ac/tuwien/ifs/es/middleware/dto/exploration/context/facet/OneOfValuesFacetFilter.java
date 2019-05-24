package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.facet;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.RDFTerm;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 *
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonTypeName("or")
public class OneOfValuesFacetFilter implements PropertyFacetFilter {

  private Resource property;
  private List<RDFTerm> values;

  public OneOfValuesFacetFilter(Resource property, List<RDFTerm> values) {
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
    return "OneOfValuesFacetFilter{" +
        "property=" + property +
        ", values=" + values +
        '}';
  }
}
