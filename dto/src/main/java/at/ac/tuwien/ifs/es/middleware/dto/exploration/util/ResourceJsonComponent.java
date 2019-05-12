package at.ac.tuwien.ifs.es.middleware.dto.exploration.util;

import static at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil.stringValue;
import static at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil.valueOf;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public final class ResourceJsonComponent {

  public static class Deserializer extends JsonDeserializer<Resource> {

    @Override
    public Resource deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      return new Resource(valueOf(p.readValueAs(String.class)));
    }
  }

  public static class Serializer extends JsonSerializer<Resource> {

    @Override
    public void serialize(Resource value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
      gen.writeString(stringValue(value.value()));
    }
  }

  public static class MapSerializer extends JsonSerializer<Resource> {

    @Override
    public void serialize(Resource value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeFieldName(value.getId());
    }
  }

  public static class MapDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
      return new Resource(key);
    }
  }

}
