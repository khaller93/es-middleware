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

@JsonComponent
public final class RDFTermJsonComponent {

  public enum TYPE {IRI, BNODE, LITERAL}

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

  private static void writeType(TYPE type, JsonGenerator generator) throws IOException {
    generator.writeStringField("@type", type.name().toLowerCase());
  }

  private static void writeIRI(IRI value, JsonGenerator generator) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else {
      generator.writeStartObject();
      writeType(TYPE.IRI, generator);
      generator.writeStringField("id", value.getIRIString());
      generator.writeEndObject();
    }
  }

  private static void writeBNode(BlankNode value, JsonGenerator generator) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else {
      generator.writeStartObject();
      writeType(TYPE.BNODE, generator);
      generator.writeStringField("id", value.ntriplesString());
      generator.writeEndObject();
    }
  }

  private static void writeLiteral(Literal value, JsonGenerator generator) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else {
      generator.writeStartObject();
      writeType(TYPE.LITERAL, generator);
      String lexicalForm = value.getLexicalForm();
      if (lexicalForm == null) {
        generator.writeNullField("value");
      } else {
        generator.writeStringField("value", lexicalForm);
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
      String type = node.get("@type").asText();
      if (TYPE.IRI.name().toLowerCase().equals(type)) {
        return rdfFactory.createIRI(node.get("id").asText());
      } else if (TYPE.BNODE.name().toLowerCase().equals(type)) {
        return rdfFactory.createBlankNode(node.get("id").asText());
      } else if (TYPE.LITERAL.name().toLowerCase().equals(type)) {
        return readLiteral(node);
      } else {
        throw new IllegalArgumentException(
            String.format("The type must be 'iri', 'bnode' or 'literal', but was %s.", type));
      }
    }
  }

  public static class LiteralDeserializer extends JsonDeserializer<Literal> {

    @Override
    public Literal deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode node = p.getCodec().readTree(p);
      String type = node.get("@type").asText();
      if (TYPE.LITERAL.name().toLowerCase().equals(type)) {
        return readLiteral(node);
      } else {
        throw new IllegalArgumentException(
            String.format("The type must be 'iri', 'bnode' or 'literal', but was %s.", type));
      }
    }
  }

  private static Literal readLiteral(JsonNode literalNode) {
    String value = literalNode.get("value").asText();
    if (literalNode.has("language")) {
      return rdfFactory.createLiteral(value, literalNode.get("language").asText());
    } else if (literalNode.has("datatype")) {
      return rdfFactory.createLiteral(value, literalNode.get("datatype").asText());
    } else {
      return rdfFactory.createLiteral(value);
    }
  }

  public static class BlankNodeOrIRIDeserializer extends JsonDeserializer<BlankNodeOrIRI> {

    @Override
    public BlankNodeOrIRI deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode node = p.getCodec().readTree(p);
      String type = node.get("@type").asText();
      if (TYPE.IRI.name().toLowerCase().equals(type)) {
        return rdfFactory.createIRI(node.get("id").asText());
      } else if (TYPE.BNODE.name().toLowerCase().equals(type)) {
        return rdfFactory.createBlankNode(node.get("id").asText());
      } else {
        throw new IllegalArgumentException(
            String.format("The type must be 'iri or 'bnode', but was %s.", type));
      }
    }
  }

  public static class IRIDeserializer extends JsonDeserializer<IRI> {

    @Override
    public IRI deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode iriNode = p.getCodec().readTree(p);
      String type = iriNode.get("@type").asText();
      if (TYPE.IRI.name().toLowerCase().equals(type)) {
        return rdfFactory.createIRI(iriNode.get("id").asText());
      } else {
        throw new IllegalArgumentException(
            String.format("The type must be 'iri', but was %s.", type));
      }
    }
  }

  public static class BNodeDeserializer extends JsonDeserializer<BlankNode> {

    @Override
    public BlankNode deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode bNode = p.getCodec().readTree(p);
      String type = bNode.get("@type").asText();
      if (TYPE.BNODE.name().toLowerCase().equals(type)) {
        return rdfFactory.createBlankNode(bNode.get("id").asText());
      } else {
        throw new IllegalArgumentException(
            String.format("The type must be 'bnode', but was %s.", type));
      }
    }
  }

}
