package at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class ResourceDeserializer extends JsonDeserializer<Resource> {

  @Override
  public Resource deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    JsonNode node = p.getCodec().readTree(p);
    if (node == null) {
      throw new IllegalArgumentException("Must not be null.");
    } else {
      if (node.isValueNode()) {
        return RDFTermJsonComponent.readResource(node.asText());
      } else if (node.isNull()) {
        return null;
      } else {
        throw new IllegalArgumentException("Resource must be specified as a string.");
      }
    }
  }

}
