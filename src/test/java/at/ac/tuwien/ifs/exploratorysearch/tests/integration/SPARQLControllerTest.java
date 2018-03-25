package at.ac.tuwien.ifs.exploratorysearch.tests.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.exploratorysearch.ExploratorySearchApplication;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.KnowledgeGraphDAO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExploratorySearchApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "es.middleware.knowledgegraph.vendor=MemoryDB"})
public class SPARQLControllerTest {

  private static Model testModel;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  @Qualifier("SpecifiedKnowledgeGraphDAO")
  private KnowledgeGraphDAO knowledgeGraphDAO;


  @BeforeClass
  public static void prepareTestDataset() throws IOException {
    try (InputStream testDatasetIn = SPARQLControllerTest.class.getResourceAsStream(
        "/datasets/musicpinta-instruments.ttl")) {
      testModel = Rio.parse(testDatasetIn, "http://leeds.ac.uk/resource/", RDFFormat.TURTLE);
    }
  }

  @Before
  public void setUp() {
    try (RepositoryConnection con = knowledgeGraphDAO.getRepository().getConnection()) {
      con.add(testModel);
    }
  }

  @Test
  public void test_dataset_loaded() throws Exception {
    try (RepositoryConnection con = knowledgeGraphDAO.getRepository().getConnection()) {
      TupleQueryResult result = con
          .prepareTupleQuery("SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE { ?s ?p ?o }").evaluate();
      assertThat("19575 distinct resources must have been loaded into the knowledgegraph.",
          Integer.parseInt(result.next().getBinding("cnt").getValue().stringValue()),
          is(19575));
    }
  }

  @Test
  public void test_countQuery_ok_mustReturnValue() throws Exception {
    // request SPARQL count query.
    ResponseEntity<String> countQueryResponse = restTemplate
        .getForEntity("/sparql?query={query}&format={format}", String.class,
            "SELECT (COUNT(DISTINCT ?s) as ?cnt) WHERE { ?s ?p ?o }",
            "application/sparql-results+json");
    assertThat("The request must be successful.",
        countQueryResponse.getStatusCode().value(), is(200));

    try (ByteArrayInputStream resultIn = new ByteArrayInputStream(
        countQueryResponse.getBody().getBytes())) {
      // parse response
      TupleQueryResult tupleQueryResult = QueryResultIO
          .parseTuple(resultIn, TupleQueryResultFormat.JSON);
      // check response
      assertTrue("Must return one result.", tupleQueryResult.hasNext());
      assertThat("The count of distinct resources must be 19575",
          Integer.parseInt(tupleQueryResult.next().getBinding("cnt").getValue().stringValue()),
          is(19575));
    }
  }

  @Test
  public void test_countQuery_wrongFormat_throwsException() throws Exception {
    ResponseEntity<String> countQueryResponse = restTemplate
        .getForEntity("/sparql?query={query}&format={format}", String.class,
            "SELECT (COUNT(?s) as ?cnt) WHERE { ?s ?p ?o }",
            "text/turtle");
    assertThat("The request must signal to have failed.",
        countQueryResponse.getStatusCode().value(), is(500));
  }

  @Test
  public void test_selectResourceQuery_ok_mustReturnMusicInstruments() throws Exception {
    ResponseEntity<String> selectQueryResponse = restTemplate
        .getForEntity("/sparql?query={query}&format={format}", String.class,
            "SELECT DISTINCT ?s WHERE { ?s a <http://purl.org/ontology/mo/Instrument> }",
            "application/sparql-results+json");
    assertThat("The request must signal to have failed.",
        selectQueryResponse.getStatusCode().value(), is(200));

    try (ByteArrayInputStream resultIn = new ByteArrayInputStream(
        selectQueryResponse.getBody().getBytes())) {
      // parse response
      List<String> instruments = QueryResults.asList(QueryResultIO
          .parseTuple(resultIn, TupleQueryResultFormat.JSON)).stream()
          .map(bs -> bs.getValue("s").stringValue())
          .collect(Collectors.toList());
      // check response
      assertThat("Must return exactly 877 results.", instruments, hasSize(877));
      assertThat("Must have the given instrument resource IRIs in the result set", instruments,
          hasItems("http://dbpedia.org/resource/Huluhu", "http://dbpedia.org/resource/Kus",
              "http://dbpedia.org/resource/Daf", "http://dbpedia.org/resource/Clavinet",
              "http://dbpedia.org/resource/Concertina"));
    }
  }

  @Test
  public void test_describeQuery_ok() throws Exception {
    ResponseEntity<String> selectQueryResponse = restTemplate
        .getForEntity("/sparql?query={query}&format={format}", String.class,
            "DESCRIBE <http://dbpedia.org/resource/Huluhu>", "text/turtle");
    assertThat("The request must signal to have failed.",
        selectQueryResponse.getStatusCode().value(), is(200));

    try (ByteArrayInputStream resultIn = new ByteArrayInputStream(
        selectQueryResponse.getBody().getBytes())) {
      Model resultModel = Rio.parse(resultIn, "test:", RDFFormat.TURTLE);
      ValueFactory valueFactory = SimpleValueFactory.getInstance();
      List<String> labels = resultModel
          .filter(valueFactory.createIRI("http://dbpedia.org/resource/Huluhu"), RDFS.LABEL, null)
          .objects().stream().map(Value::stringValue).collect(Collectors.toList());
      assertThat("There is only one label for 'Huluhu' in the test data", labels, hasSize(1));
      assertThat("The label must be 'Huluhu'.", labels, hasItem("Huluhu"));
      List<String> descriptions = resultModel
          .filter(valueFactory.createIRI("http://dbpedia.org/resource/Huluhu"), RDFS.COMMENT, null)
          .objects().stream().map(Value::stringValue).collect(
              Collectors.toList());
      assertThat("There is only one description for 'Huluhu' in the test data", descriptions,
          hasSize(1));
      assertThat("The label must be 'Huluhu'.", descriptions.get(0), containsString(
          "The huluhu is a Chinese bowed string instrument in the huqin family of instruments."));
      List<String> subjects = resultModel
          .filter(valueFactory.createIRI("http://dbpedia.org/resource/Huluhu"), DCTERMS.SUBJECT,
              null)
          .objects().stream().map(Value::stringValue).collect(
              Collectors.toList());
      assertThat("'Huluhu' has four subjects according to the test data.", subjects,
          containsInAnyOrder("http://dbpedia.org/resource/Category:Necked_bowl_lutes",
              "http://dbpedia.org/resource/Category:Huqin_family_instruments",
              "http://dbpedia.org/resource/Category:Chinese_musical_instruments",
              "http://dbpedia.org/resource/Category:Bowed_instruments"));
    }
  }

  @Test
  public void test_askForUnknownInstrument_mustReturnFalse() throws Exception {
    ResponseEntity<String> selectQueryResponse = restTemplate
        .getForEntity("/sparql?query={query}&format={format}", String.class,
            "ASK WHERE { ?s a <http://purl.org/ontology/mo/Instrument> ; rdfs:label \"Jaguar\"@en .}",
            "text/boolean");
    assertThat("The request must signal to have failed.",
        selectQueryResponse.getStatusCode().value(), is(200));
    assertThat("The response must be false, because there is no instrument for 'Jaguar'.",
        selectQueryResponse.getBody(), is("false"));
  }

  @Test
  public void test_askForWellKnownInstrument_mustReturnTrue() throws Exception {
    ResponseEntity<String> selectQueryResponse = restTemplate
        .getForEntity("/sparql?query={query}&format={format}", String.class,
            "ASK WHERE { ?s a <http://purl.org/ontology/mo/Instrument> ; rdfs:label \"Harp\"@en .}",
            "text/boolean");
    assertThat("The request must signal to have failed.",
        selectQueryResponse.getStatusCode().value(), is(200));
    assertThat("The response must be true, because there is a harp resource in the test data.",
        selectQueryResponse.getBody(), is("true"));
  }

  @Test
  public void test_updateInsertData_mustBeSuccessful() throws Exception {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();
    ResponseEntity<Void> updateDataResponse = restTemplate
        .postForEntity("/sparql/update?query={query}", null, Void.class,
            "INSERT DATA { <test:a> a <http://purl.org/ontology/mo/Instrument> ; rdfs:label \"A\" ; rdfs:comment \"A test instance.\" . }");
    assertThat("Insert-Update must be successful.",
        updateDataResponse.getStatusCode().value(), is(200));
    try (RepositoryConnection con = knowledgeGraphDAO.getRepository().getConnection()) {
      assertTrue(con.hasStatement(valueFactory.createIRI("test:a"), RDF.TYPE,
          valueFactory.createIRI("http://purl.org/ontology/mo/Instrument"), false));
      assertTrue(con.hasStatement(valueFactory.createIRI("test:a"), RDFS.LABEL,
          valueFactory.createLiteral("A"), false));
      assertTrue(con.hasStatement(valueFactory.createIRI("test:a"), RDFS.COMMENT,
          valueFactory.createLiteral("A test instance."), false));
    }
  }

  @Test
  public void test_updateInsertDataWithInvalidIRI_mustRespondWithFailure() throws Exception {
    ResponseEntity<Void> updateDataResponse = restTemplate
        .postForEntity("/sparql/update?query={query}", null, Void.class,
            "INSERT DATA { <:a/\\path> a <http://purl.org/ontology/mo/Instrument>. }");
    assertThat("Insert-Update must fail with 500, due to invalid IRI.",
        updateDataResponse.getStatusCode().value(), is(500));
  }

  @Test
  public void test_updateDeleteData_mustBeSuccessful() throws Exception {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();
    ResponseEntity<Void> updateDataResponse = restTemplate
        .postForEntity("/sparql/update?query={query}", null, Void.class,
            "DELETE WHERE { <http://dbpedia.org/resource/Huluhu> ?p1 ?o .}");
    assertThat("Delete-Update must be successful.",
        updateDataResponse.getStatusCode().value(), is(200));
    try (RepositoryConnection con = knowledgeGraphDAO.getRepository().getConnection()) {
      assertFalse(
          con.hasStatement(valueFactory.createIRI("http://dbpedia.org/resource/Huluhu"), null, null,
              false));
    }
  }

  @After
  public void tearDown() {
    try (RepositoryConnection con = knowledgeGraphDAO.getRepository().getConnection()) {
      con.clear();
    }
  }

}
