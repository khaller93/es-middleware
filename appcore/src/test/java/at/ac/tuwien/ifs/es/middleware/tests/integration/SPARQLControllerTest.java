package at.ac.tuwien.ifs.es.middleware.tests.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.ExploratorySearchApplication;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Before;
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

/**
 * This class integration tests {@link at.ac.tuwien.ifs.es.middleware.controller.SPARQLController}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExploratorySearchApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
    "esm.analysis.computeOnStart=false",
    "esm.db.gremlin.syncOnStart=false",
    "esm.db.map=memory",
})
public class SPARQLControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;
  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;

  @Before
  public void before() throws Exception {
    wineOntologyDatasetResource
        .before(Lists.newArrayList(KGSparqlDAO.class.getName()), Collections.emptyList());
  }

  @Test
  public void test_countWinesQuery_ok_mustReturnValue() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers
        .setAccept(Collections.singletonList(MediaType.valueOf("application/sparql-results+json")));
    // request SPARQL count query.
    ResponseEntity<String> countQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers), String.class,
            "SELECT (COUNT(DISTINCT ?s) as ?cnt) WHERE { ?s a  <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Winery> .}");
    assertThat("The request must be successful.",
        countQueryResponse.getStatusCode().value(), is(200));

    try (ByteArrayInputStream resultIn = new ByteArrayInputStream(
        countQueryResponse.getBody().getBytes())) {
      // parse response
      TupleQueryResult tupleQueryResult = QueryResultIO
          .parseTuple(resultIn, TupleQueryResultFormat.JSON);
      // check response
      assertTrue("Must return one result.", tupleQueryResult.hasNext());
      assertThat("The count of distinct wines must be 43.",
          Integer.parseInt(tupleQueryResult.next().getBinding("cnt").getValue().stringValue()),
          is(43));
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
            "SELECT DISTINCT ?s WHERE { ?s a/rdfs:subClassOf* <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine> }");
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
      assertThat("Must return exactly 4 results.", instruments, hasSize(4));
      assertThat("Must have the given instrument resource IRIs in the result set", instruments,
          hasItems("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne",
              "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling",
              "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
              "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"));
    }
  }

  @Test
  public void test_askForUnknownInstrument_mustReturnFalse() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.valueOf("text/boolean")));
    ResponseEntity<String> selectQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers), String.class,
            "ASK WHERE { ?s a <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Winery> ; rdfs:label \"Wine ABC\"@en .}");
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
            "ASK WHERE { ?wine <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#hasSugar> <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sweet> .}");
    assertThat("The request must signal to have failed.",
        selectQueryResponse.getStatusCode().value(), is(200));
    assertThat("The response must be true, because there is a sweet wine in the test data.",
        selectQueryResponse.getBody(), is("true"));
  }

  @Test
  public void test_updateInsertData_mustBeSuccessful() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("update",
        "INSERT DATA { <test:a> a <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine> ; rdfs:label \"A\" ; rdfs:comment \"A test instance.\" . }");
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
          valueFactory.createIRI("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine")));
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
    map.add("update",
        "INSERT DATA { <:a/\\path> a <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling>. }");
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

    ResponseEntity<Void> updateDataResponse = restTemplate
        .exchange("/sparql/update", HttpMethod.POST, entity, Void.class);
    assertThat("Insert-Update must fail with 500, due to invalid IRI.",
        updateDataResponse.getStatusCode().value(), is(500));
  }

  @Test
  public void test_updateDeleteData_mustBeSuccessful() throws Exception {

    // Check if wine instance exists
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.valueOf("text/boolean")));
    ResponseEntity<String> askQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers), String.class,
            "ASK WHERE { <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling> ?p ?o }");
    assertThat("Wine 'SchlossVolradTrochenbierenausleseRiesling' must be in the dataset.",
        askQueryResponse.getBody(), is("true"));

    // Delete the wine instance
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("update",
        "DELETE WHERE { <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling> <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#hasWineDescriptor> ?o .}");
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

    ResponseEntity<Void> updateDataResponse = restTemplate
        .exchange("/sparql/update", HttpMethod.POST, entity, Void.class);
    assertThat("Delete-Update must be successful.",
        updateDataResponse.getStatusCode().value(), is(200));

    // Check if update was persisted.
    headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.valueOf("text/boolean")));
    ResponseEntity<String> askResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers),
            String.class,
            "ASK WHERE { <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling> <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#hasWineDescriptor> ?o .}");
    assertThat("The 'SchlossVolradTrochenbierenausleseRiesling' resource must be removed.",
        askResponse.getBody(), is("false"));
  }

  @Test
  public void test_updateOperationOnWrongEndpoint_mustFailWithCode400() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.valueOf("text/boolean")));
    ResponseEntity<String> selectQueryResponse = restTemplate
        .exchange("/sparql?query={query}", HttpMethod.GET, new HttpEntity<>(headers),
            String.class,
            "DELETE WHERE { <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Winery> ?p1 ?o .}");
    assertThat("The request must signal to have failed with 400 (Bad Request).",
        selectQueryResponse.getStatusCode().value(), is(400));
  }

}
