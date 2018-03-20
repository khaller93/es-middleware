package at.ac.tuwien.ifs.exploratorysearch.tests.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.exploratorysearch.ExploratorySearchApplication;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.RDF4JMemoryKnowledgeGraph;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
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
  public void test_selectResourceQuery_ok_mustReturn100MusicInstruments() throws Exception {
    ResponseEntity<String> selectQueryResponse = restTemplate
        .getForEntity("/sparql?query={query}&format={format}", String.class,
            "SELECT DISTINCT ?s WHERE { ?s ?p ?o } LIMIT 100",
            "application/sparql-results+json");
    System.out.println(selectQueryResponse.getBody());
    assertThat("The request must signal to have failed.",
        selectQueryResponse.getStatusCode().value(), is(200));

    try (ByteArrayInputStream resultIn = new ByteArrayInputStream(
        selectQueryResponse.getBody().getBytes())) {
      // parse response
      List<BindingSet> bindingSets = QueryResults.asList(QueryResultIO
          .parseTuple(resultIn, TupleQueryResultFormat.JSON));
      // check response
      assertThat("Must return exactly 100 results, due to 'LIMIT'.", bindingSets, hasSize(100));
    }
  }

  @After
  public void tearDown() {
    try (RepositoryConnection con = knowledgeGraphDAO.getRepository().getConnection()) {
      con.clear();
    }
  }

}
