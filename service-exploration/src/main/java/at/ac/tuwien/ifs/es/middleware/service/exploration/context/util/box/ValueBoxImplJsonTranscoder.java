package at.ac.tuwien.ifs.es.middleware.service.exploration.context.util.box;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;

/**
 * This class provides serializer and deserializer implementations for {@link ValueBoxImpl}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
final class ValueBoxImplJsonTranscoder {

  static final TypeReference<Map<String, JsonNode>> typeRef
      = new TypeReference<Map<String, JsonNode>>() {
  };

  static class Serializer extends JsonSerializer<ValueBoxImpl> {

    @Override
    public void serialize(ValueBoxImpl value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value != null) {
        Map<String, JsonNode> jsonNodeMap = value.asMap();
        if (jsonNodeMap != null) {
          gen.writeObject(jsonNodeMap);
        } else {
          gen.writeNull();
        }
      } else {
        gen.writeNull();
      }
    }
  }

  static class Deserializer extends JsonDeserializer<ValueBoxImpl> {

    @Override
    public ValueBoxImpl deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      return new ValueBoxImpl(p.readValueAs(typeRef));
    }
  }

}
