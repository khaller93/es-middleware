package at.ac.tuwien.ifs.es.middleware.tests.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.ExploratorySearchApplication;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
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
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExploratorySearchApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "esm.db.choice=IndexedMemoryDB",
    "esm.fts.choice=IndexedMemoryDB",
    "esm.cache.enable=false"
})
public class SPARQLControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;
  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;

  @Test
  public void test_countQuery_ok_mustReturnValue() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers
        .setAccept(Collections.singletonList(MediaType.valueOf("application/sparql-results+json")));
    // request SPARQL count query.
    ResponseEntity<String> countQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers), String.class,
            "SELECT (COUNT(DISTINCT ?s) as ?cnt) WHERE { ?s ?p ?o }");
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
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.valueOf("text/turtle")));
    ResponseEntity<String> countQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers), String.class,
            "SELECT (COUNT(?s) as ?cnt) WHERE { ?s ?p ?o }");
    assertThat("The request must signal to have failed.",
        countQueryResponse.getStatusCode().value(), is(406));
  }

  @Test
  public void test_selectResourceQuery_ok_mustReturnMusicInstruments() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers
        .setAccept(Collections.singletonList(MediaType.valueOf("application/sparql-results+json")));
    ResponseEntity<String> selectQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers), String.class,
            "SELECT DISTINCT ?s WHERE { ?s a <http://purl.org/ontology/mo/Instrument> }");
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
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.valueOf("text/turtle")));
    ResponseEntity<String> selectQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers), String.class,
            "DESCRIBE <http://dbpedia.org/resource/Huluhu>");
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
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.valueOf("text/boolean")));
    ResponseEntity<String> selectQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers), String.class,
            "ASK WHERE { ?s a <http://purl.org/ontology/mo/Instrument> ; rdfs:label \"Jaguar\"@en .}");
    assertThat("The request must signal to have failed.",
        selectQueryResponse.getStatusCode().value(), is(200));
    assertThat("The response must be false, because there is no instrument for 'Jaguar'.",
        selectQueryResponse.getBody(), is("false"));
  }

  @Test
  public void test_askForWellKnownInstrument_mustReturnTrue() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.valueOf("text/boolean")));
    ResponseEntity<String> selectQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers),
            String.class,
            "ASK WHERE { ?s a <http://purl.org/ontology/mo/Instrument> ; rdfs:label \"Harp\"@en .}");
    assertThat("The request must signal to have failed.",
        selectQueryResponse.getStatusCode().value(), is(200));
    assertThat("The response must be true, because there is a harp resource in the test data.",
        selectQueryResponse.getBody(), is("true"));
  }

  @Test
  public void test_updateInsertData_mustBeSuccessful() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("update",
        "INSERT DATA { <test:a> a <http://purl.org/ontology/mo/Instrument> ; rdfs:label \"A\" ; rdfs:comment \"A test instance.\" . }");
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

    ValueFactory valueFactory = SimpleValueFactory.getInstance();
    ResponseEntity<Void> updateDataResponse = restTemplate
        .exchange("/sparql/update", HttpMethod.POST, entity, Void.class);
    assertThat("Insert-Update must be successful.",
        updateDataResponse.getStatusCode().value(), is(200));

    // Check if update was persisted.
    HttpHeaders describeHeaders = new HttpHeaders();
    describeHeaders.setAccept(Collections.singletonList(MediaType.valueOf("text/turtle")));
    ResponseEntity<String> selectQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(describeHeaders),
            String.class, "DESCRIBE <test:a>");
    assertThat("The request must signal to have failed.",
        selectQueryResponse.getStatusCode().value(), is(200));
    try (ByteArrayInputStream resultIn = new ByteArrayInputStream(
        selectQueryResponse.getBody().getBytes())) {
      Model testModel = Rio.parse(resultIn, "test:", RDFFormat.TURTLE);
      IRI testIRI = valueFactory.createIRI("test:a");
      assertTrue(testModel.contains(testIRI, RDF.TYPE,
          valueFactory.createIRI("http://purl.org/ontology/mo/Instrument")));
      assertTrue(testModel.contains(testIRI, RDFS.LABEL, valueFactory.createLiteral("A")));
      assertTrue(testModel
          .contains(testIRI, RDFS.COMMENT, valueFactory.createLiteral("A test instance.")));
    }
  }

  @Test
  public void test_updateInsertDataWithInvalidIRI_mustRespondWithFailure() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("update", "INSERT DATA { <:a/\\path> a <http://purl.org/ontology/mo/Instrument>. }");
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

    ResponseEntity<Void> updateDataResponse = restTemplate
        .exchange("/sparql/update", HttpMethod.POST, entity, Void.class);
    assertThat("Insert-Update must fail with 500, due to invalid IRI.",
        updateDataResponse.getStatusCode().value(), is(500));
  }

  @Test
  public void test_updateDeleteData_mustBeSuccessful() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("update", "DELETE WHERE { <http://dbpedia.org/resource/Huluhu> ?p1 ?o .}");
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

    ResponseEntity<Void> updateDataResponse = restTemplate
        .exchange("/sparql/update", HttpMethod.POST, entity, Void.class);
    assertThat("Delete-Update must be successful.",
        updateDataResponse.getStatusCode().value(), is(200));

    //Check if update was persisted.
    HttpHeaders askHeaders = new HttpHeaders();
    askHeaders.setAccept(Collections.singletonList(MediaType.valueOf("text/boolean")));
    ResponseEntity<String> askResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(askHeaders),
            String.class, "ASK WHERE { <http://dbpedia.org/resource/Huluhu> ?p ?o }");
    assertThat("The 'Huluhu' resource must be removed.", askResponse.getBody(), is("false"));
  }

  @Test
  public void test_updateOperationOnWrongEndpoint_mustFailWithCode400() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.valueOf("text/boolean")));
    ResponseEntity<String> selectQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers),
            String.class,
            "DELETE WHERE { <http://dbpedia.org/resource/Huluhu> ?p1 ?o .}");
    assertThat("The request must signal to have failed with 400 (Bad Request).",
        selectQueryResponse.getStatusCode().value(), is(400));
  }

}
