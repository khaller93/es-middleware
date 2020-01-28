package at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.serializer;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import java.io.IOException;

public class ResourceAsMapKeyDeserializer extends KeyDeserializer {

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
    return new Resource(key);
  }
}
