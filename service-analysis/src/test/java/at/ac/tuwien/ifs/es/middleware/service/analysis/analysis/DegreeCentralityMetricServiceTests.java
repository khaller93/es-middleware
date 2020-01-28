package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree.DegreeCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.degree.DegreeCentralityMetricWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
    ThreadPoolConfig.class, PrimaryKGDAOConfig.class, RDF4JDAOConfig.class, ThreadPoolConfig.class,
    MapDBDummy.class, WineOntologyDatasetResource.class,
    SimpleSPARQLService.class, AllResourcesWithSPARQLService.class,
    DAOScheduler.class, SchedulerPipeline.class, MapDBDummy.class, DAODependencyGraphService.class,
    DegreeCentralityMetricWithGremlinService.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class DegreeCentralityMetricServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private DegreeCentralityMetricService degreeCentralityMetricService;
  @Autowired
  private AllResourcesWithSPARQLService allResourcesWithSPARQLService;

  @Before
  public void setUp() throws Exception {
    allResourcesWithSPARQLService.compute();
    degreeCentralityMetricService.compute();
  }

  @Test
  public void computeDegreeMetrics_mustReturnDegree() {
    assertThat(degreeCentralityMetricService.getValueFor(
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SweetRiesling")),
        is(2L));
    assertThat(degreeCentralityMetricService.getValueFor(
        new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermel")),
        is(1L));
  }

  @Test
  public void computeDegreeMetricsAndGetForUnknownResource_mustReturnNull() {
    Long distanceForUnknownResource = degreeCentralityMetricService
        .getValueFor(new Resource("test:a"));
    assertNull(distanceForUnknownResource);
  }

  @Test(expected = IllegalArgumentException.class)
  public void computeDegreeMetricsAndGetForNull_mustThrowIllegalArgumentException() {
    degreeCentralityMetricService.getValueFor(null);
  }

}
