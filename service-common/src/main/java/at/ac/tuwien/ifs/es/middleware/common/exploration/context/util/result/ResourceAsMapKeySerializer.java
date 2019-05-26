package at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class ResourceAsMapKeySerializer extends JsonSerializer<Resource> {

  @Override
  public void serialize(Resource value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeFieldName(value.getId());
  }
}
