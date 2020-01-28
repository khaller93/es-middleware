package at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.serializer;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.RDFLiteral;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.RDFValueTerm;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

/**
 * This class provides {@link JsonSerializer} and {@link JsonDeserializer} for {@link RDFValueTerm}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class RDFTermJsonComponent {

  private static RDF rdfFactory = new SimpleRDF();

  static void writeResource(Resource value, JsonGenerator generator) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else {
      generator.writeString(RDFTermJsonUtil.stringValue(value.value()));
    }
  }

  static void writeLiteral(RDFLiteral value, JsonGenerator generator) throws IOException {
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

  static Resource readResource(String text) {
    if (text.startsWith("_:")) {
      return new Resource(rdfFactory.createBlankNode(text.substring(2)));
    } else {
      return new Resource(rdfFactory.createIRI(text));
    }
  }

  static RDFLiteral readLiteral(JsonNode literalNode) {
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

}
