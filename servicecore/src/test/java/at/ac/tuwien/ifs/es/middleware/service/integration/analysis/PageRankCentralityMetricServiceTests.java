package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNull;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
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
 * This class implements integration test cases for {@link PageRankCentralityMetricService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, ThreadPoolConfig.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class PageRankCentralityMetricServiceTests {

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
  private ApplicationContext context;

  private PageRankCentralityMetricService pageRankCentralityMetricService;

  @PostConstruct
  public void setUpInstance() {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Before
  public void setUp() throws InterruptedException {
    musicPintaResource.waitForAllDAOsBeingReady();
    pageRankCentralityMetricService = new PageRankCentralityMetricWithGremlinService(gremlinService,
        context, taskExecutor);
  }

  @Test
  public void computePageRankForAllResources_mustReturnCorrespondingPageRank() {
    pageRankCentralityMetricService.compute();
    List<Resource> resources = ((SelectQueryResult) sparqlDAO
        .query(
            "SELECT distinct ?resource WHERE { {?resource ?p1 ?o} UNION {?s ?p2 ?resource} . FILTER(isIRI(?resource))}",
            false)).value().stream().map(r -> new Resource((BlankNodeOrIRI) r.get("resource")))
        .collect(Collectors.toList());
    List<Pair<Resource, Double>> resourceList = resources.stream().map(
        resource -> new ImmutablePair<>(resource,
            pageRankCentralityMetricService.getValueFor(resource)))
        .sorted((e1, e2) -> -Double.compare(e1.getRight(), e2.getRight()))
        .collect(Collectors.toList());
    assertThat("Instrument class must be in the top 10.", resourceList.stream().limit(10)
            .map(r -> r.getKey().getId()).collect(Collectors.toList()),
        hasItem("http://purl.org/ontology/mo/Instrument"));
    assertThat(resourceList.stream().map(Pair::getRight).collect(Collectors.toList()),
        not(hasItem(equalTo(null))));
  }

  @Test
  public void computePageRankForAllResources_mustReturnNullForUnknownResource() {
    pageRankCentralityMetricService.compute();
    assertNull(pageRankCentralityMetricService.getValueFor(new Resource("test:a")));
  }

}
