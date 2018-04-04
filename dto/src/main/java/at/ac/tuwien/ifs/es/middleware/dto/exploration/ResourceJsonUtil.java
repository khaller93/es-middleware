package at.ac.tuwien.ifs.es.middleware.dto.exploration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class ResourceJsonUtil {

  private static RDF valueFactory = new SimpleRDF();

  public static class Deserializer extends JsonDeserializer<BlankNodeOrIRI> {

    @Override
    public BlankNodeOrIRI deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      return valueOf(p.readValueAs(String.class));
    }
  }

  public static class Serializer extends JsonSerializer<BlankNodeOrIRI> {

    private RDF valueFactory = new SimpleRDF();

    @Override
    public void serialize(BlankNodeOrIRI value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
      gen.writeString(stringValue(value));
    }
  }

  public static BlankNodeOrIRI valueOf(String value) {
    if (value.startsWith("_:")) {
      return valueFactory.createBlankNode(value.replace("_:", ""));
    } else {
      return valueFactory.createIRI(value);
    }
  }

  public static String stringValue(BlankNodeOrIRI resource) {
    if (resource instanceof IRI) {
      return ((IRI)resource).getIRIString();
    } else {
      return "_:" + ((BlankNode)resource).uniqueReference();
    }
  }

}
