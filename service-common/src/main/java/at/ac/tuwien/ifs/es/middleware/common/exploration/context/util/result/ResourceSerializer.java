package at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class ResourceSerializer extends JsonSerializer<Resource> {

  @Override
  public void serialize(Resource value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    if (value == null) {
      gen.writeNull();
    } else {
      RDFTermJsonComponent.writeResource(value, gen);
    }
  }
}
