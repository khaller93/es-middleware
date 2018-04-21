package at.ac.tuwien.ifs.es.middleware.service.integration;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.MalformedSPARQLQueryException;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.AskQueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.GraphQueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.QueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.sparql.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import com.google.common.collect.Table;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.api.Triple;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MusicPintaInstrumentsResource.class,
    KnowledgeGraphConfig.class, SimpleSPARQLService.class, IndexedMemoryKnowledgeGraph.class})
@TestPropertySource(properties = {
    "esm.db.choice=IndexedMemoryDB",
    "esm.fts.choice=IndexedMemoryDB",
})
public class SimpleSPARQLServiceTest {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;

  @Autowired
  public SPARQLService sparqlService;

  @Test
  public void test_countQuery_ok_mustReturnValue() throws Exception {
    QueryResult result = sparqlService
        .query("SELECT (COUNT(DISTINCT ?s) as ?cnt) WHERE { ?s ?p ?o }", false);
    assertThat("The result must be of a 'select' query.", result,
        instanceOf(SelectQueryResult.class));
    List<Map<String, RDFTerm>> resultTable = ((SelectQueryResult) result).value();
    assertThat("The result table must have only one entry.", resultTable.size(), is(1));
    RDFTerm countTerm = resultTable.get(0).get("cnt");
    assertNotNull(countTerm);
    assertThat("The given count value must be a literal.", countTerm, instanceOf(Literal.class));
    Literal countLiteral = (Literal) countTerm;
    assertThat("", Integer.parseInt(countLiteral.getLexicalForm()), is(19575));
  }

  @Test
  public void test_selectResourceQuery_ok_mustReturnMusicInstruments() throws Exception {
    QueryResult result = sparqlService
        .query("SELECT DISTINCT ?s WHERE { ?s a <http://purl.org/ontology/mo/Instrument> }", false);
    assertThat("The result must be of a 'select' query.", result,
        instanceOf(SelectQueryResult.class));
    List<Map<String, RDFTerm>> resultTable = ((SelectQueryResult) result).value();
    assertTrue("The table must contain a column.", resultTable.get(0).containsKey("s"));
    List<RDFTerm> resources = resultTable.stream().map(r -> r.get("s"))
        .collect(Collectors.toList());
    assertThat("The number of returned distinct resources must be 877.", resources, hasSize(877));
    List<String> resourceIRIs = resources.stream().filter(s -> s instanceof IRI)
        .map(s -> ((IRI) s).getIRIString()).collect(Collectors.toList());
    assertThat("Must have the given instrument resource IRIs in the result set", resourceIRIs,
        hasItems("http://dbpedia.org/resource/Huluhu", "http://dbpedia.org/resource/Kus",
            "http://dbpedia.org/resource/Daf", "http://dbpedia.org/resource/Clavinet",
            "http://dbpedia.org/resource/Concertina"));
  }

  @Test(expected = MalformedSPARQLQueryException.class)
  public void test_executeUpdateQueryOnQueryMethod_throwMalformedSPARQLQueryException()
      throws Exception {
    sparqlService.query(
        "INSERT DATA { <test:a> a <http://purl.org/ontology/mo/Instrument> ; rdfs:label \"A\" ; rdfs:comment \"A test instance.\" . }",
        false);
  }

  @Test
  public void test_askForUnknownInstrument_mustReturnFalse() throws Exception {
    QueryResult result = sparqlService.query(
        "ASK WHERE { ?s a <http://purl.org/ontology/mo/Instrument> ; rdfs:label \"Jaguar\"@en .}",
        false);
    assertThat("The result must be of an 'ask' query.", result, instanceOf(AskQueryResult.class));
    AskQueryResult askQueryResult = (AskQueryResult) result;
    assertFalse(
        "The 'ask' query must evaluate to false, there is no instrument named 'Jaguar' in the knowledgegraph.",
        askQueryResult.value());
  }

  @Test
  public void test_askForWellKnownInstrument_mustReturnTrue() throws Exception {
    QueryResult result = sparqlService.query(
        "ASK WHERE { ?s a <http://purl.org/ontology/mo/Instrument> ; rdfs:label \"Harp\"@en .}",
        false);
    assertThat("The result must be of an 'ask' query.", result, instanceOf(AskQueryResult.class));
    AskQueryResult askQueryResult = (AskQueryResult) result;
    assertTrue("The response must be true, because there is a harp resource in the test data.",
        askQueryResult.value());
  }

  @Test
  public void test_describeQuery_ok() throws Exception {
    RDF valueFactory = new SimpleRDF();
    QueryResult result = sparqlService
        .query("DESCRIBE <http://dbpedia.org/resource/Huluhu>", false);
    assertThat("", result, instanceOf(GraphQueryResult.class));
    Graph resultGraph = ((GraphQueryResult) result).value();
    IRI huluhuIRI = valueFactory.createIRI("http://dbpedia.org/resource/Huluhu");
    assertTrue(resultGraph.contains(huluhuIRI, null, null));
    assertThat("There is only one label and it must be to 'Huluhu'.",
        resultGraph
            .stream(huluhuIRI, valueFactory.createIRI("http://www.w3.org/2000/01/rdf-schema#label"),
                null).map(Triple::getObject)
            .filter(l -> l instanceof Literal).map(l -> ((Literal) l).getLexicalForm())
            .collect(Collectors.toList()), contains("Huluhu"));
    assertThat("There is only one description for the instrument 'Huluhu'.",
        resultGraph
            .stream(huluhuIRI,
                valueFactory.createIRI("http://www.w3.org/2000/01/rdf-schema#comment"),
                null).map(Triple::getObject)
            .filter(l -> l instanceof Literal).map(l -> ((Literal) l).getLexicalForm())
            .collect(Collectors.toList()), contains(
            "The huluhu is a Chinese bowed string instrument in the huqin family of instruments. It has two strings, and its sound box is made from a gourd, with a face made of thin wood. It is used primarily by the Zhuang people of the southern Chinese province of Guangxi. The instrument's name is derived from the Chinese words húlú (\"gourd\") and hú (short for huqin)."));
    assertThat("'Huluhu' has four subjects according to the test data.", resultGraph
            .stream(huluhuIRI, valueFactory.createIRI("http://purl.org/dc/terms/subject"), null)
            .map(Triple::getObject).filter(r -> r instanceof IRI).map(r -> ((IRI) r).getIRIString())
            .collect(Collectors.toList()),
        containsInAnyOrder("http://dbpedia.org/resource/Category:Necked_bowl_lutes",
            "http://dbpedia.org/resource/Category:Huqin_family_instruments",
            "http://dbpedia.org/resource/Category:Chinese_musical_instruments",
            "http://dbpedia.org/resource/Category:Bowed_instruments"));

  }

  @Test
  public void test_updateInsertData_mustBeSuccessful() throws Exception {
    RDF valueFactory = new SimpleRDF();
    sparqlService.update(
        "INSERT DATA { <test:a> a <http://purl.org/ontology/mo/Instrument> ; rdfs:label \"A\" ; rdfs:comment \"A test instance.\" . }");
    GraphQueryResult result = (GraphQueryResult) sparqlService.query("Describe <test:a>", false);
    Graph resultGraph = result.value();
    IRI testIRI = valueFactory.createIRI("test:a");
    assertTrue(resultGraph.contains(testIRI, null, null));
    assertTrue(resultGraph
        .contains(testIRI,
            valueFactory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            valueFactory.createIRI("http://purl.org/ontology/mo/Instrument")));
    assertTrue(resultGraph
        .contains(testIRI, valueFactory.createIRI("http://www.w3.org/2000/01/rdf-schema#label"),
            valueFactory.createLiteral("A")));
    assertTrue(resultGraph
        .contains(testIRI, valueFactory.createIRI("http://www.w3.org/2000/01/rdf-schema#comment"),
            valueFactory.createLiteral("A test instance.")));
  }

  @Test(expected = KnowledgeGraphSPARQLException.class)
  public void test_updateInsertDataWithInvalidIRI_mustRespondWithFailure() throws Exception {
    sparqlService.update("INSERT DATA { <:a/\\path> a <http://purl.org/ontology/mo/Instrument>. }");
  }

  @Test
  public void test_updateDeleteData_mustBeSuccessful() throws Exception {
    RDF valueFactory = new SimpleRDF();
    sparqlService.update("DELETE WHERE { <http://dbpedia.org/resource/Huluhu> ?p1 ?o .}");
    GraphQueryResult result = (GraphQueryResult) sparqlService
        .query("DESCRIBE <http://dbpedia.org/resource/Huluhu>", false);
    Graph resultGraph = result.value();
    IRI testIRI = valueFactory.createIRI("http://dbpedia.org/resource/Huluhu");
    assertFalse(resultGraph.contains(testIRI, null, null));
  }

}
