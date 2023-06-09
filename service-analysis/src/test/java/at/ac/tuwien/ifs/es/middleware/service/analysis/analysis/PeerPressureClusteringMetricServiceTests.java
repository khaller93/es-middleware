package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAODependencyGraphService;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAOScheduler;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.PrimaryKGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.scheduler.SchedulerPipeline;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.peerpressure.PeerPressureClusteringMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.peerpressure.PeerPressureClusteringMetricWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.testutil.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import java.math.BigDecimal;
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
 * This class tests {@link PeerPressureClusteringMetricService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, SimpleGremlinService.class,
    RDF4JLuceneFullTextSearchDAO.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    ClonedInMemoryGremlinDAO.class, ThreadPoolConfig.class, PrimaryKGDAOConfig.class,
    RDF4JDAOConfig.class,
    ThreadPoolConfig.class, SpringCacheConfig.class, MapDBDummy.class,
    WineOntologyDatasetResource.class, PeerPressureClusteringMetricWithGremlinService.class,
    DAOScheduler.class, SchedulerPipeline.class, MapDBDummy.class, DAODependencyGraphService.class,
    AllResourcesWithSPARQLService.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class PeerPressureClusteringMetricServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private AllResourcesService allResourcesService;
  @Autowired
  private PeerPressureClusteringMetricService peerPressureClusteringMetricService;

  @Before
  public void setUp() throws Exception {
    allResourcesService.compute();
    peerPressureClusteringMetricService.compute();
  }

  @Test
  public void computeThePeerPressureAndGetItForSameResourcesPair_mustReturnTrue() {
    ResourcePair resourcePair = ResourcePair.of(
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes"),
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes"));
    DecimalNormalizedAnalysisValue valueObj = peerPressureClusteringMetricService.isSharingSameCluster(
        resourcePair);
    assertNotNull(valueObj);
    assertThat(valueObj.getValue().doubleValue(), is(1.0));

  }

  @Test
  public void computeThePeerPressureAndGetItForTwoResourcesInDifferentCLuster_mustReturnFalse() {
    ResourcePair resourcePair = ResourcePair.of(
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes"),
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauMargauxWinery"));
    DecimalNormalizedAnalysisValue valueObj = peerPressureClusteringMetricService.isSharingSameCluster(
        resourcePair);
    assertThat(valueObj.getValue().doubleValue(), is(0.0));
  }

  @Test
  public void computeThePeerPressureAndGetItForUnknownPair_mustReturnNull() {
    ResourcePair resourcePair = ResourcePair.of(new Resource("test:a"),
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes"));
    DecimalNormalizedAnalysisValue valueObj = peerPressureClusteringMetricService.isSharingSameCluster(
        resourcePair);
    assertNull(valueObj);
  }

  @Test
  public void computeThePeerPressureAndGetItForUnknownPair2_mustReturnNull() {
    ResourcePair resourcePair = ResourcePair.of(
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes"),
        new Resource("test:a"));
    DecimalNormalizedAnalysisValue valueObj = peerPressureClusteringMetricService.isSharingSameCluster(
        resourcePair);
    assertNull(valueObj);
  }

  @Test
  public void computeThePeerPressureAndGetItForUnknownPair3_mustReturnNull() {
    ResourcePair resourcePair = ResourcePair.of(new Resource("test:b"),
        new Resource("test:a"));
    DecimalNormalizedAnalysisValue valueObj = peerPressureClusteringMetricService.isSharingSameCluster(
        resourcePair);
    assertNull(valueObj);
  }

  @Test(expected = IllegalArgumentException.class)
  public void computeThePeerPressureForNull_mustThrowIllegalArgument() {
    peerPressureClusteringMetricService.isSharingSameCluster(null);
  }
}
