package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree.DegreeCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree.DegreeCentralityMetricWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class implements integration test cases for {@link DegreeCentralityMetricService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, ThreadPoolConfig.class,
    AnalysisPipelineProcessorDummy.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class DegreeCentralityMetricServiceTests {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  public KGSparqlDAO sparqlDAO;
  @Autowired
  public KGGremlinDAO gremlinDAO;
  @Autowired
  public TaskExecutor taskExecutor;
  @Autowired
  public GremlinService gremlinService;
  @Autowired
  private AnalysisPipelineProcessor processor;

  private DegreeCentralityMetricService degreeCentralityMetricService;

  @PostConstruct
  public void setUpInstance() {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Before
  public void setUp() throws InterruptedException {
    musicPintaResource.waitForAllDAOsBeingReady();
    degreeCentralityMetricService = new DegreeCentralityMetricWithGremlinService(gremlinService,
        processor);
  }

  @Test
  public void computeDegreeMetricsAndGetForTop10_mustBeEqualToSPARQLResult() {
    degreeCentralityMetricService.compute();
    List<Resource> resources = (sparqlDAO
        .<SelectQueryResult>query(
            "SELECT distinct ?resource WHERE { {?resource ?p1 ?o} UNION {?s ?p2 ?resource} . FILTER(isIRI(?resource))}",
            false)).value().stream().map(r -> new Resource((BlankNodeOrIRI) r.get("resource")))
        .collect(Collectors.toList());
    List<Map<String, RDFTerm>> result = ((SelectQueryResult) sparqlDAO
        .query("select ?o (count(*) as ?c) where { \n"
            + "    ?s ?p ?o .\n"
            + "    FILTER (isIRI(?o)) .\n"
            + "}\n"
            + "GROUP BY ?o\n"
            + "ORDER BY DESC(?c)\n"
            + "LIMIT 10", true)).value();
    List<Long> sparqlDegreeCount = new LinkedList<>();
    List<Long> gremlinDegreeCount = new LinkedList<>();
    for (Map<String, RDFTerm> row : result) {
      sparqlDegreeCount.add(Long.parseLong(((Literal) row.get("c")).getLexicalForm()));
      gremlinDegreeCount
          .add(degreeCentralityMetricService.getValueFor(new Resource((IRI) row.get("o"))));
    }
    assertThat(gremlinDegreeCount, hasItems(sparqlDegreeCount.toArray(new Long[0])));
  }

  @Test
  public void computeDegreeMetricsAndGetForUnknownResource_mustReturnNull() {
    degreeCentralityMetricService.compute();
    Long distanceForUnknownResource = degreeCentralityMetricService
        .getValueFor(new Resource("test:a"));
    assertNull(distanceForUnknownResource);
  }

  @Test
  public void computeDegreeMetricsAndGetForNewResource_mustReturnOnlineComputedResult() {
    degreeCentralityMetricService.compute();
    PGS schema = gremlinService.getPropertyGraphSchema();
    Vertex vertexA = gremlinService.traversal().getGraph()
        .addVertex(schema.iri().identifier(), "test:a", schema.kind().identifier(), "iri",
            "version", Instant.now().getEpochSecond());
    Vertex vertexB = gremlinService.traversal().getGraph()
        .addVertex(schema.iri().identifier(), "test:b", schema.kind().identifier(), "iri",
            "version", Instant.now().getEpochSecond());
    vertexA.addEdge("owl:sameAs", vertexB);
    Long resourceA = degreeCentralityMetricService.getValueFor(new Resource("test:a"));
    assertNotNull(resourceA);
    assertThat(resourceA, equalTo(1L));
  }
}
