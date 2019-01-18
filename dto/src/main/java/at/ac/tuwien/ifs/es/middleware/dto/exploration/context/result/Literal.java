package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class Literal implements RDFTerm {

  private static final RDF rdf = new SimpleRDF();

  private final String literal;
  private final String languageTag;
  private final String datatype;

  public Literal(String literal) {
    this(literal, null, null);
  }

  @JsonCreator
  public Literal(@JsonProperty(value = "literal", required = true) String literal,
      @JsonProperty(value = "languageTag") String languageTag,
      @JsonProperty(value = "datatype") String datatype) {
    checkArgument(literal != null, "The literal string must not be null.");
    this.literal = literal;
    this.languageTag = languageTag;
    this.datatype = datatype;
  }

  public org.apache.commons.rdf.api.Literal value() {
    if (languageTag == null && datatype == null) {
      return rdf.createLiteral(this.literal);
    } else if (languageTag != null) {
      return rdf.createLiteral(this.literal, languageTag);
    } else {
      return rdf.createLiteral(this.literal, datatype);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Literal literal1 = (Literal) o;
    return literal.equals(literal1.literal) &&
        Objects.equals(languageTag, literal1.languageTag) &&
        Objects.equals(datatype, literal1.datatype);
  }

  @Override
  public int hashCode() {
    return Objects.hash(literal, languageTag, datatype);
  }

  @Override
  public String toString() {
    return "Literal{" +
        "literal='" + literal + '\'' +
        ", languageTag='" + languageTag + '\'' +
        ", datatype='" + datatype + '\'' +
        '}';
  }
}
