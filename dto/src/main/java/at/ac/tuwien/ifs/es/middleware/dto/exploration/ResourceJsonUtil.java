package at.ac.tuwien.ifs.es.middleware.dto.exploration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class ResourceJsonUtil {

  private static ValueFactory valueFactory = SimpleValueFactory.getInstance();

  public static class Deserializer extends JsonDeserializer<Resource> {

    @Override
    public Resource deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      return resourceOf(p.readValueAs(String.class));
    }
  }

  public static class Serializer extends JsonSerializer<Resource> {

    @Override
    public void serialize(Resource value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
      gen.writeString(value.toString());
    }
  }

  public static Resource resourceOf(String value) {
    if (value.startsWith("_:")) {
      return valueFactory.createBNode(value.replace("_:", ""));
    } else {
      return valueFactory.createIRI(value);
    }
  }
}
