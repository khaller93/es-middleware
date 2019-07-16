package at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.RDFValueTerm;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;

/**
 * This class is a custom {@link JsonDeserializer} for {@link RDFValueTerm} superclass. It delegates
 * the control to the corresponding deserializer (IRI, BNODE, LITERAL).
 */
@JsonComponent
public class RDFTermDeserializer extends JsonDeserializer<RDFValueTerm> {

  @Override
  public RDFValueTerm deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    JsonNode node = p.getCodec().readTree(p);
    if (node == null) {
      throw new IllegalArgumentException("Must not be null.");
    } else {
      if (node.isValueNode()) { // IRI or blank node
        return RDFTermJsonComponent.readResource(node.asText());
      } else if (node.isObject()) { // RDFLiteral
        return RDFTermJsonComponent.readLiteral(node);
      } else if (node.isNull()) {
        return null;
      } else {
        throw new IllegalArgumentException(
            "A valid IRI, blanknode string or literal object must be given.");
      }
    }
  }
}
