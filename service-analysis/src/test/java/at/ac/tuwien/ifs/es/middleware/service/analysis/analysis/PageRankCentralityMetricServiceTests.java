package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAODependencyGraphService;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAOScheduler;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.PrimaryKGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.scheduler.SchedulerPipeline;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.CentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.testutil.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
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
    ThreadPoolConfig.class, PrimaryKGDAOConfig.class, RDF4JDAOConfig.class, ThreadPoolConfig.class,
    AllResourcesWithSPARQLService.class, MapDBDummy.class, WineOntologyDatasetResource.class,
    DAOScheduler.class, SchedulerPipeline.class, MapDBDummy.class, DAODependencyGraphService.class,
    SimpleSPARQLService.class, PageRankCentralityMetricWithGremlinService.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class PageRankCentralityMetricServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private AllResourcesService allResourcesService;
  @Autowired
  private PageRankCentralityMetricService pageRankCentralityMetricService;

  @Before
  public void setUp() throws Exception {
    allResourcesService.compute();
    pageRankCentralityMetricService.compute();
  }

  @Test
  public void computePageRankForSpecificResources_mustReturnCorrespondingPageRank() {
    DecimalNormalizedAnalysisValue specialWinePR = pageRankCentralityMetricService
        .getValueFor(new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"));
    assertNotNull(specialWinePR);
    DecimalNormalizedAnalysisValue winePR = pageRankCentralityMetricService
        .getValueFor(new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"));
    assertNotNull(winePR);
    assertThat(winePR.getValue().doubleValue(),
        greaterThan(specialWinePR.getValue().doubleValue()));
  }

  @Test
  public void computePageRankForAllResources_mustReturnNullForUnknownResource() {
    assertNull(pageRankCentralityMetricService.getValueFor(new Resource("test:a")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void computePageRankForNullResource_mustThrowIllegalArgumentException() {
    pageRankCentralityMetricService.getValueFor(null);
  }
}
