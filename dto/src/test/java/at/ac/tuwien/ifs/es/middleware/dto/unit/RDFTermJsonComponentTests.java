package at.ac.tuwien.ifs.es.middleware.dto.unit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.RDFTermJsonComponent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RDFTermJsonComponentTests {

  private SimpleRDF simpleRDF = new SimpleRDF();
  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(RDFTerm.class, new RDFTermJsonComponent.RDFTermSerializer());
    module.addSerializer(IRI.class, new RDFTermJsonComponent.IRISerializer());
    module.addSerializer(BlankNode.class, new RDFTermJsonComponent.BlankNodeSerializer());
    module.addSerializer(Literal.class, new RDFTermJsonComponent.LiteralSerializer());
    objectMapper.registerModule(module);
  }

  @Test
  public void writeIRI_mustReturnJSON() throws JsonProcessingException {
    String iriString = objectMapper.writer().writeValueAsString(simpleRDF.createIRI("test://a"));
    assertThat(iriString, containsString("\"@type\":\"iri\""));
    assertThat(iriString, containsString("\"id\":\"test://a\""));
  }

  @Test
  public void writeBlankNode_mustReturnJSON() throws JsonProcessingException {
    BlankNode blankNode = simpleRDF.createBlankNode();
    String bnodeString = objectMapper.writer().writeValueAsString(blankNode);
    assertThat(bnodeString, containsString("\"@type\":\"bnode\""));
    assertThat(bnodeString, containsString("\"id\":\"" + blankNode.ntriplesString() + "\""));
  }

  @Test
  public void writeBasicLiteral_mustReturnJSON() throws JsonProcessingException {
    Literal literal = simpleRDF.createLiteral("25");
    String literalString = objectMapper.writer().writeValueAsString(literal);
    assertThat(literalString, containsString("\"@type\":\"literal\""));
    assertThat(literalString, containsString("\"value\":\"25\""));
    assertThat(literalString,
        containsString("\"datatype\":\"http://www.w3.org/2001/XMLSchema#string\""));
    assertThat(literalString, not(containsString("\"language\"")));
  }

  @Test
  public void writeLnguageLiteral_mustReturnJSON() throws JsonProcessingException {
    Literal literal = simpleRDF.createLiteral("Haus", "de");
    String literalString = objectMapper.writer().writeValueAsString(literal);
    assertThat(literalString, containsString("\"@type\":\"literal\""));
    assertThat(literalString, containsString("\"value\":\"Haus\""));
    assertThat(literalString,
        containsString("\"datatype\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#langString\""));
    assertThat(literalString, containsString("\"language\":\"de\""));
  }

  @Ignore
  @Test
  public void readIRI_mustReturnIRIObject() throws IOException {
    IRI iriObj = objectMapper.reader().readValue("{\"@type\":\"iri\",\"id\":\"test://b\"}");
    assertNotNull(iriObj);
    assertThat(iriObj.getIRIString(), is("test://b"));
  }

}
