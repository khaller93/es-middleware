package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.neighbourhood;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(use = Id.NONE)
public abstract class RDFTerm implements IdentifiableResult {

  /**
   * @return {@link RDFTerm}
   */
  public static RDFTerm of(org.apache.commons.rdf.api.RDFTerm value) {
    checkArgument(value != null, "The value must not be null.");
    if (value instanceof BlankNodeOrIRI) {
      return new Resource((BlankNodeOrIRI) value);
    } else if (value instanceof Literal) {
      return new RDFLiteral((Literal) value);
    } else {
      throw new IllegalArgumentException(String
          .format("The class '%s' cannot be mapped to an RDF Term.", value.getClass().getName()));
    }
  }

}
