package at.ac.tuwien.ifs.es.middleware.testutil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class implements generic tests for the SPARQL interface of {@link KnowledgeGraphDAO}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    MusicPintaInstrumentsResource.class
})
public class AbstractMusicPintaGremlinTests {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaInstrumentsResource;
  @Autowired
  private KnowledgeGraphDAO knowledgeGraphDAO;

  @Test
  public void test_getResource_mustBeInGremlinGraph() throws Exception {
    assertThat(knowledgeGraphDAO.getGremlinDAO().traversal()
        .V("http://dbtune.org/musicbrainz/resource/instrument/132").toList(), hasSize(1));
  }

  @Test
  public void test_getCategoriesOfInstrument117_mustReturnAllCategories() throws Exception {
    List<Vertex> categoriesGremlin = knowledgeGraphDAO.getGremlinDAO().traversal()
        .V("http://dbtune.org/musicbrainz/resource/instrument/117")
        .out("http://purl.org/dc/terms/subject").toList();
    assertThat(categoriesGremlin.stream().map(c -> (String) c.id()).collect(Collectors.toList()),
        containsInAnyOrder("http://dbpedia.org/resource/Category:Mexican_musical_instruments",
            "http://dbpedia.org/resource/Category:Guitar_family_instruments"));
  }

  @Test
  public void test_countAllInstruments_mustReturnCorrectResults() throws Exception {
    long instrumentsCount = Long.parseLong(((Literal) (((SelectQueryResult) knowledgeGraphDAO
        .query("select (COUNT(DISTINCT ?s) AS ?cnt) { \n"
            + " ?s a <http://purl.org/ontology/mo/Instrument> .\n"
            + "}", true)).value().get(0).get("cnt"))).getLexicalForm());
    assertThat(
        knowledgeGraphDAO.getGremlinDAO().traversal().V("http://purl.org/ontology/mo/Instrument")
            .inE().outV().dedup().count().next(), is(equalTo(instrumentsCount)));
  }

  @Test
  public void test_getCountIndividuals_mustReturnCorrectNumber() throws Exception {
    long totalResourceNumber = Long.parseLong(((Literal) ((SelectQueryResult) knowledgeGraphDAO
        .query("SELECT (count(DISTINCT ?s) AS ?cnt) WHERE {\n"
            + "  {?s ?p ?o}\n"
            + "  UNION\n"
            + "  {?z ?b ?s}\n"
            + "  FILTER(!isLiteral(?s))\n"
            + "}", true)).value().get(0).get("cnt")).getLexicalForm());
    assertThat("Each resource (not-literal) must be represented as vertex in the gremlin graph.",
        knowledgeGraphDAO.getGremlinDAO().traversal().V().count().next(),
        is(equalTo(totalResourceNumber)));
  }

  @Test
  @Ignore
  public void test_countAllClassesWithInstances_mustReturnCorrectNumber() throws Exception {
    List<String> classesWithInstancesSPARQL = (((SelectQueryResult) knowledgeGraphDAO
        .query("select DISTINCT ?clazz { \n"
            + " ?s a ?clazz\n"
            + "}", true)).value()).stream()
        .map(r -> BlankOrIRIJsonUtil.stringValue((BlankNodeOrIRI) r.get("clazz")))
        .collect(Collectors.toList());
    List<Vertex> classesWithInstancesGremlin = knowledgeGraphDAO.getGremlinDAO().traversal().V()
        .outE("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").inV().dedup().toList();
    System.out.println(classesWithInstancesGremlin.stream().map(v -> (String) v.id())
        .collect(Collectors.toList()));
    assertThat(
        classesWithInstancesGremlin.stream().map(v -> (String) v.id()).collect(Collectors.toList()),
        containsInAnyOrder(classesWithInstancesSPARQL));
  }
}
