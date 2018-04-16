package at.ac.tuwien.ifs.es.middleware.dto.exploration.util;

import static at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil.stringValue;
import static at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil.valueOf;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class ResourceJsonUtil {

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

}
