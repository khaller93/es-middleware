package at.ac.tuwien.ifs.es.middleware.dto.exploration.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public final class RDFTermJsonComponent {

  public enum TYPE {IRI, BNODE, LITERAL}

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
      return null;
    }
  }

}
