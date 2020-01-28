package at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.serializer;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.RDFLiteral;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class LiteralDeserializer extends JsonDeserializer<RDFLiteral> {

  @Override
  public RDFLiteral deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    JsonNode node = p.getCodec().readTree(p);
    if (node == null) {
      throw new IllegalArgumentException("Must not be null.");
    } else {
      if (node.isObject()) {
        return RDFTermJsonComponent.readLiteral(node);
      } else if (node.isNull()) {
        return null;
      } else {
        throw new IllegalArgumentException("A valid literal object must be given");
      }
    }
  }
}
