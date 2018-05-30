package at.ac.tuwien.ifs.es.middleware.testutil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class implements generic tests for the SPARQL interface of {@link KnowledgeGraphDAO}s. The
 * method {@link AbstractMusicPintaGremlinTests#getSparqlDAO()} must return the tested
 * {@link KnowledgeGraphDAO}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractMusicPintaGremlinTests {

  private MusicPintaInstrumentsResource musicPintaInstrumentsResource;
  private KGSparqlDAO sparqlDAO;
  private KGGremlinDAO gremlinDAO;

  /**
   * gets the {@link KnowledgeGraphDAO} that shall be tested.
   */
  public abstract KnowledgeGraphDAO getSparqlDAO();

  @Before
  public void setUp() throws Throwable {
    KnowledgeGraphDAO knowledgeGraphDAO = getSparqlDAO();
    this.sparqlDAO = knowledgeGraphDAO.getSparqlDAO();
    this.gremlinDAO = knowledgeGraphDAO.getGremlinDAO();
    this.musicPintaInstrumentsResource = new MusicPintaInstrumentsResource(knowledgeGraphDAO);
    this.musicPintaInstrumentsResource.before();
  }

  @After
  public void tearDown() throws Throwable {
    this.musicPintaInstrumentsResource.after();
  }

  @Test
  public void test_getResource_mustBeInGremlinGraph() throws Exception {
    assertThat(gremlinDAO.traversal()
        .V("http://dbtune.org/musicbrainz/resource/instrument/132").toList(), hasSize(1));
  }

  @Test
  public void test_getCategoriesOfInstrument117_mustReturnAllCategories() throws Exception {
    List<Vertex> categoriesGremlin = gremlinDAO.traversal()
        .V("http://dbtune.org/musicbrainz/resource/instrument/117")
        .out("http://purl.org/dc/terms/subject").toList();
    assertThat(categoriesGremlin.stream().map(c -> (String) c.id()).collect(Collectors.toList()),
        containsInAnyOrder("http://dbpedia.org/resource/Category:Mexican_musical_instruments",
            "http://dbpedia.org/resource/Category:Guitar_family_instruments"));
  }

  @Test
  public void test_countAllInstruments_mustReturnCorrectResults() throws Exception {
    long instrumentsCount = Long.parseLong(((Literal) (((SelectQueryResult) sparqlDAO
        .query("select (COUNT(DISTINCT ?s) AS ?cnt) { \n"
            + " ?s a <http://purl.org/ontology/mo/Instrument> .\n"
            + "}", true)).value().get(0).get("cnt"))).getLexicalForm());
    assertThat(
        gremlinDAO.traversal().V("http://purl.org/ontology/mo/Instrument")
            .inE().outV().dedup().count().next(), is(equalTo(instrumentsCount)));
  }

  @Test
  public void test_getCountIndividuals_mustReturnCorrectNumber() throws Exception {
    long totalResourceNumber = Long.parseLong(((Literal) ((SelectQueryResult) sparqlDAO
        .query("SELECT (count(DISTINCT ?s) AS ?cnt) WHERE {\n"
            + "  {?s ?p ?o}\n"
            + "  UNION\n"
            + "  {?z ?b ?s}\n"
            + "  FILTER(!isLiteral(?s))\n"
            + "}", true)).value().get(0).get("cnt")).getLexicalForm());
    assertThat("Each resource (not-literal) must be represented as vertex in the gremlin graph.",
        gremlinDAO.traversal().V().count().next(),
        is(equalTo(totalResourceNumber)));
  }

  @Test
  @Ignore
  public void test_countAllClassesWithInstances_mustReturnCorrectNumber() throws Exception {
    List<String> classesWithInstancesSPARQL = (((SelectQueryResult) sparqlDAO
        .query("select DISTINCT ?clazz { \n"
            + " ?s a ?clazz\n"
            + "}", true)).value()).stream()
        .map(r -> BlankOrIRIJsonUtil.stringValue((BlankNodeOrIRI) r.get("clazz")))
        .collect(Collectors.toList());
    List<Vertex> classesWithInstancesGremlin = gremlinDAO.traversal().V()
        .outE("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").inV().dedup().toList();
    System.out.println(classesWithInstancesGremlin.stream().map(v -> (String) v.id())
        .collect(Collectors.toList()));
    assertThat(
        classesWithInstancesGremlin.stream().map(v -> (String) v.id()).collect(Collectors.toList()),
        containsInAnyOrder(classesWithInstancesSPARQL));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_getAllClasses_mustHaveInstrumentAndPerformanceClass() {
    GraphTraversalSource g = gremlinDAO.traversal();
    List<Vertex> classes = g.V()
        .union(__.inE("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").inV(),
            __.as("c").out("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
                .V("http://www.w3.org/2000/01/rdf-schema#Class").select("c")).dedup().toList();
    List<String> classStrings = classes.stream().map(v -> (String) v.id())
        .collect(Collectors.toList());
    assertThat(classStrings, hasItems("http://purl.org/ontology/mo/Instrument",
        "http://purl.org/ontology/mo/Performance"));
  }

  @Test(timeout = 60000)
  @SuppressWarnings("unchecked")
  public void test_getAllSubClassesOfGuitar() {
    GraphTraversalSource g = gremlinDAO.traversal();
    List<Object> guitarSubClasses = g.V("http://dbpedia.org/resource/Guitar").as("c")
        .repeat(__.in("http://www.w3.org/2000/01/rdf-schema#subClassOf"))
        .until(__.or(__.not(__.in("http://www.w3.org/2000/01/rdf-schema#subClassOf")),
            __.cyclicPath())).path().unfold().dedup().toList();
    List<String> guitarSubClassStrings = guitarSubClasses.stream()
        .map(v -> ((String) ((Vertex) v).id())).collect(Collectors.toList());
    assertThat(guitarSubClassStrings, hasItems("http://dbpedia.org/resource/Electric_guitar",
        "http://dbpedia.org/resource/Classical_guitar",
        "http://dbtune.org/musicbrainz/resource/instrument/467"));
  }
}
