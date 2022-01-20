package at.ac.tuwien.ifs.es.middleware.testutil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.Literal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * This class implements generic tests for the SPARQL interface of {@link KGGremlinDAO}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class AbstractMusicPintaGremlinTests {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaInstrumentsResource;
  @Autowired
  private KGSparqlDAO sparqlDAO;
  @Autowired
  private KGGremlinDAO gremlinDAO;

  protected PGS schema;

  @Before
  public void setUp() throws Exception {
    this.schema = gremlinDAO.getPropertyGraphSchema();
  }

  @Test
  public void test_getResource_mustBeInGremlinGraph() throws Exception {
    assertThat(gremlinDAO.traversal()
        .V().has(schema.iri().identifierAsString(),
            "http://dbtune.org/musicbrainz/resource/instrument/132").toList(), hasSize(1));
  }

  @Test
  @Ignore
  public void test_getCategoriesOfInstrument117_mustReturnAllCategories() throws Exception {
    List<Vertex> categoriesGremlin = gremlinDAO.traversal()
        .V().has(schema.iri().identifierAsString(),
            "http://dbtune.org/musicbrainz/resource/instrument/117")
        .out("http://purl.org/dc/terms/subject").toList();
    assertThat(categoriesGremlin.stream()
            .map(elem -> schema.iri().<String>apply(elem))
            .collect(Collectors.toList()),
        containsInAnyOrder("http://dbpedia.org/resource/Category:Mexican_musical_instruments",
            "http://dbpedia.org/resource/Category:Guitar_family_instruments"));
  }

  @Test
  public void test_countAllInstruments_mustReturnCorrectResults() throws Exception {
    long instrumentsCount = Long.parseLong(((Literal) ((sparqlDAO
        .<SelectQueryResult>query("select (COUNT(DISTINCT ?s) AS ?cnt) { \n"
            + " ?s a <http://purl.org/ontology/mo/Instrument> .\n"
            + "}", true)).value().get(0).get("cnt"))).getLexicalForm());
    assertThat(
        gremlinDAO.traversal().V()
            .has(schema.iri().identifierAsString(), "http://purl.org/ontology/mo/Instrument")
            .inE().outV().dedup().count().next(), is(equalTo(instrumentsCount)));
  }

  @Test
  public void test_getCountIndividuals_mustReturnCorrectNumber() throws Exception {
    long totalResourceNumber = Long.parseLong(((Literal) (sparqlDAO
        .<SelectQueryResult>query("SELECT (count(DISTINCT ?s) AS ?cnt) WHERE {\n"
            + "   {\n"
            + "        ?s ?p ?o .\n"
            + "    } UNION {\n"
            + "        ?z ?b ?s .\n"
            + "    }\n"
            + "    FILTER(isIRI(?s)) .\n"
            + "}", true)).value().get(0).get("cnt")).getLexicalForm());
    assertThat("Each resource (not-literal) must be represented as vertex in the gremlin graph.",
        gremlinDAO.traversal().V().dedup().count().next(), is(equalTo(totalResourceNumber)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_getAllClasses_mustHaveInstrumentAndPerformanceClass() {
    GraphTraversalSource g = gremlinDAO.traversal();
    List<Vertex> classes = g.V()
        .union(__.inE("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").inV(),
            __.as("c").out("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
                .V().has(schema.iri().identifierAsString(),
                "http://www.w3.org/2000/01/rdf-schema#Class").select("c")).dedup()
        .toList();
    List<String> classStrings = classes.stream()
        .map(elem -> schema.iri().<String>apply(elem)).collect(Collectors.toList());
    assertThat(classStrings, hasItems("http://purl.org/ontology/mo/Instrument",
        "http://purl.org/ontology/mo/Performance"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_getAllSubClassesOfGuitar() {
    GraphTraversalSource g = gremlinDAO.traversal();
    List<Object> guitarSubClasses = g.V()
        .has(schema.iri().identifierAsString(), "http://dbpedia.org/resource/Guitar").as("c")
        .repeat(__.in("http://www.w3.org/2000/01/rdf-schema#subClassOf"))
        .until(__.or(__.not(__.in("http://www.w3.org/2000/01/rdf-schema#subClassOf")),
            __.cyclicPath())).path().unfold().dedup().toList();
    List<String> guitarSubClassStrings = guitarSubClasses.stream()
        .map(v -> schema.iri().<String>apply((Vertex) v))
        .collect(Collectors.toList());
    assertThat(guitarSubClassStrings, hasItems("http://dbpedia.org/resource/Electric_guitar",
        "http://dbpedia.org/resource/Classical_guitar",
        "http://dbtune.org/musicbrainz/resource/instrument/467"));
  }

}
