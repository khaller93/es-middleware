package at.ac.tuwien.ifs.es.middleware.facet;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;

/**
 *
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class OneOfValuesFacetFilter implements PropertyFacetFilter {

  private IRI property;
  private List<RDFTerm> values;

  public OneOfValuesFacetFilter(IRI property, List<RDFTerm> values) {
    checkArgument(property != null, "The property must be specified for a facet.");
    checkArgument(values != null && !values.isEmpty(),
        "At least one value must be given for the or facet.");
    this.property = property;
    this.values = values;
  }

  public IRI getProperty() {
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
