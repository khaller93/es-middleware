package at.ac.tuwien.ifs.es.middleware.dto.exploration.util;

import static at.ac.tuwien.ifs.es.middleware.dto.exploration.util.RDFTermJsonUtil.stringValue;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.neighbourhood.RDFLiteral;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.neighbourhood.RDFTerm;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.springframework.boot.jackson.JsonComponent;

/**
 * This class provides {@link JsonSerializer} and {@link JsonDeserializer} for {@link RDFTerm}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonComponent
public final class ContextRDFTermJsonComponent {

  private static RDF rdfFactory = new SimpleRDF();

  /**
   * This class is a custom {@link JsonSerializer} for {@link RDFTerm} superclass. It delegates the
   * control to the corresponding serializer (IRI, BNODE, LITERAL).
   */
  public static class RDFTermSerializer extends JsonSerializer<RDFTerm> {

    @Override
    public void serialize(RDFTerm value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        if (value instanceof Resource) {
          writeResource((Resource) value, gen);
        } else if (value instanceof RDFLiteral) {
          writeLiteral((RDFLiteral) value, gen);
        } else {
          throw new IllegalArgumentException(
              String.format("Given RDFTerm subclass '%s' is unknown.",
                  value.getClass().getSimpleName()));
        }
      }
    }
  }

  public static class ResourceSerializer extends JsonSerializer<Resource> {

    @Override
    public void serialize(Resource value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        writeResource(value, gen);
      }
    }
  }

  public static class LiteralSerializer extends JsonSerializer<RDFLiteral> {

    @Override
    public void serialize(RDFLiteral value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        writeLiteral(value, gen);
      }
    }
  }

  private static void writeResource(Resource value, JsonGenerator generator) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else {
      generator.writeString(stringValue(value.value()));
    }
  }

  private static void writeLiteral(RDFLiteral value, JsonGenerator generator) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else {
      generator.writeStartObject();
      generator.writeStringField("id", value.getId());
      String lexicalForm = value.value().getLexicalForm();
      if (lexicalForm == null) {
        generator.writeNullField("literal");
      } else {
        generator.writeStringField("literal", lexicalForm);
      }
      IRI datatype = value.value().getDatatype();
      if (datatype != null) {
        generator.writeStringField("datatype", datatype.getIRIString());
      }
      Optional<String> languageTag = value.value().getLanguageTag();
      if (languageTag != null && languageTag.isPresent()) {
        generator.writeStringField("language", languageTag.get());
      }
      generator.writeEndObject();
    }
  }

  private static Resource readResource(String text) {
    if (text.startsWith("_:")) {
      return new Resource(rdfFactory.createBlankNode(text.substring(2)));
    } else {
      return new Resource(rdfFactory.createIRI(text));
    }
  }

  /**
   * This class is a custom {@link JsonDeserializer} for {@link RDFTerm} superclass. It delegates
   * the control to the corresponding deserializer (IRI, BNODE, LITERAL).
   */
  public static class RDFTermDeserializer extends JsonDeserializer<RDFTerm> {

    @Override
    public RDFTerm deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode node = p.getCodec().readTree(p);
      if (node == null) {
        throw new IllegalArgumentException("Must not be null.");
      } else {
        if (node.isValueNode()) { // IRI or blank node
          return readResource(node.asText());
        } else if (node.isObject()) { // RDFLiteral
          return readLiteral(node);
        } else if (node.isNull()) {
          return null;
        } else {
          throw new IllegalArgumentException(
              "A valid IRI, blanknode string or literal object must be given.");
        }
      }
    }
  }

  public static class LiteralDeserializer extends JsonDeserializer<RDFLiteral> {

    @Override
    public RDFLiteral deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode node = p.getCodec().readTree(p);
      if (node == null) {
        throw new IllegalArgumentException("Must not be null.");
      } else {
        if (node.isObject()) {
          return readLiteral(node);
        } else if (node.isNull()) {
          return null;
        } else {
          throw new IllegalArgumentException("A valid literal object must be given");
        }
      }
    }
  }

  private static RDFLiteral readLiteral(JsonNode literalNode) {
    if (!literalNode.has("literal")) {
      throw new IllegalArgumentException("The literal object must specify a value.");
    } else {
      String id = literalNode.get("id").asText();
      String value = literalNode.get("literal").asText();
      if (literalNode.has("language")) {
        return new RDFLiteral(
            rdfFactory.createLiteral(value, literalNode.get("language").asText()));
      } else if (literalNode.has("datatype")) {
        return new RDFLiteral(rdfFactory.createLiteral(value, literalNode.get("datatype").asText()));
      } else {
        return new RDFLiteral(rdfFactory.createLiteral(value));
      }
    }
  }

  public static class ResourceDeserializer extends JsonDeserializer<Resource> {

    @Override
    public Resource deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode node = p.getCodec().readTree(p);
      if (node == null) {
        throw new IllegalArgumentException("Must not be null.");
      } else {
        if (node.isValueNode()) {
          return readResource(node.asText());
        } else if (node.isNull()) {
          return null;
        } else {
          throw new IllegalArgumentException("Resource must be specified as a string.");
        }
      }
    }
  }

}
