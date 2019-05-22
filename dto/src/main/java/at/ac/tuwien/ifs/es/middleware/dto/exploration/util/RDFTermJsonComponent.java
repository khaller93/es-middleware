package at.ac.tuwien.ifs.es.middleware.dto.exploration.util;

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
import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFTerm;
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
public final class RDFTermJsonComponent {

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
        if (value instanceof IRI) {
          writeIRI((IRI) value, gen);
        } else if (value instanceof BlankNode) {
          writeBNode((BlankNode) value, gen);
        } else if (value instanceof Literal) {
          writeLiteral((Literal) value, gen);
        } else {
          throw new IllegalArgumentException(
              String.format("Given RDFTerm subclass '%s' is unknown.",
                  value.getClass().getSimpleName()));
        }
      }
    }
  }

  public static class IRISerializer extends JsonSerializer<IRI> {

    @Override
    public void serialize(IRI value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        writeIRI(value, gen);
      }
    }
  }

  public static class BlankNodeSerializer extends JsonSerializer<BlankNode> {

    @Override
    public void serialize(BlankNode value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        writeBNode(value, gen);
      }
    }
  }

  public static class LiteralSerializer extends JsonSerializer<Literal> {

    @Override
    public void serialize(Literal value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        writeLiteral(value, gen);
      }
    }
  }

  private static void writeIRI(IRI value, JsonGenerator generator) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else {
      generator.writeString(value.getIRIString());
    }
  }

  private static void writeBNode(BlankNode value, JsonGenerator generator) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else {
      generator.writeString(value.ntriplesString());
    }
  }

  private static void writeLiteral(Literal value, JsonGenerator generator) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else {
      generator.writeStartObject();
      String lexicalForm = value.getLexicalForm();
      if (lexicalForm == null) {
        generator.writeNullField("literal");
      } else {
        generator.writeStringField("literal", lexicalForm);
      }
      IRI datatype = value.getDatatype();
      if (datatype != null) {
        generator.writeStringField("datatype", datatype.getIRIString());
      }
      Optional<String> languageTag = value.getLanguageTag();
      if (languageTag != null && languageTag.isPresent()) {
        generator.writeStringField("language", languageTag.get());
      }
      generator.writeEndObject();
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
          String text = node.asText();
          if (text.startsWith("_:")) {
            return rdfFactory.createBlankNode(text.substring(2));
          } else {
            return rdfFactory.createIRI(text);
          }
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

  public static class LiteralDeserializer extends JsonDeserializer<Literal> {

    @Override
    public Literal deserialize(JsonParser p, DeserializationContext ctxt)
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

  private static Literal readLiteral(JsonNode literalNode) {
    if (!literalNode.has("literal")) {
      throw new IllegalArgumentException("The literal object must specify a value.");
    } else {
      String value = literalNode.get("literal").asText();
      if (literalNode.has("language")) {
        return rdfFactory.createLiteral(value, literalNode.get("language").asText());
      } else if (literalNode.has("datatype")) {
        return rdfFactory.createLiteral(value, literalNode.get("datatype").asText());
      } else {
        return rdfFactory.createLiteral(value);
      }
    }
  }

  public static class BlankNodeOrIRIDeserializer extends JsonDeserializer<BlankNodeOrIRI> {

    @Override
    public BlankNodeOrIRI deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode node = p.getCodec().readTree(p);
      if (node.isValueNode()) {
        String idNode = node.asText();
        if (idNode.startsWith("_:")) {
          return rdfFactory.createBlankNode(idNode.substring(2));
        } else {
          return rdfFactory.createIRI(idNode);
        }
      } else if (node.isNull()) {
        return null;
      } else {
        throw new IllegalArgumentException("Blank node must be specified as a string (e.g. _:a).");
      }
    }
  }

  public static class IRIDeserializer extends JsonDeserializer<IRI> {

    @Override
    public IRI deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode node = p.getCodec().readTree(p);
      if (node == null) {
        throw new IllegalArgumentException("Must not be null.");
      } else {
        if (node.isValueNode()) {
          return rdfFactory.createIRI(node.asText());
        } else if (node.isNull()) {
          return null;
        } else {
          throw new IllegalArgumentException("IRI must be specified as a string.");
        }
      }
    }
  }

  public static class BNodeDeserializer extends JsonDeserializer<BlankNode> {

    @Override
    public BlankNode deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode node = p.getCodec().readTree(p);
      if (node == null) {
        throw new IllegalArgumentException("Must not be null.");
      } else {
        if (node.isValueNode()) {
          String bnode = node.asText();
          if (bnode.startsWith("_:")) {
            return rdfFactory.createBlankNode(bnode.substring(2));
          } else {
            throw new IllegalArgumentException("Blank node must start with '_:'.");
          }
        } else if (node.isNull()) {
          return null;
        } else {
          throw new IllegalArgumentException(
              "Blank node must be specified as a string (e.g. _:a).");
        }
      }
    }
  }

}
