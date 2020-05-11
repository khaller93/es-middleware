package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.comparesEqualTo;
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
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd.LDSDWithSPARQLMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd.LinkedDataSemanticDistanceMetricService;
import at.ac.tuwien.ifs.es.middleware.testutil.MapDBDummy;
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
 * This class tests {@link LDSDWithSPARQLMetricService} that is unweighted.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PrimaryKGDAOConfig.class, RDF4JDAOConfig.class,
    ClonedInMemoryGremlinDAO.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    RDF4JLuceneFullTextSearchDAO.class, ThreadPoolConfig.class, LDSDWithSPARQLMetricService.class,
    SimpleSPARQLService.class, MapDBDummy.class, SpringCacheConfig.class,
    WineOntologyDatasetResource.class, AllResourcesWithSPARQLService.class,
    DAOScheduler.class, SchedulerPipeline.class, MapDBDummy.class,
    DAODependencyGraphService.class,})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class UnweightedLDSDMetricServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private AllResourcesService allResourcesService;
  @Autowired
  private LinkedDataSemanticDistanceMetricService ldsdMetricService;

  @Before
  public void setUp() throws Exception {
    allResourcesService.compute();
    ldsdMetricService.compute();
  }

  @Test
  public void computeLDSDAndGetItForSameWine_mustReturnValidResult() {
    Resource sauternesResource = new Resource(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes");
    Resource sweetRieslingResource = new Resource(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes");
    DecimalNormalizedAnalysisValue ldsdValue = ldsdMetricService
        .getValueFor(ResourcePair.of(sauternesResource, sweetRieslingResource));
    assertNotNull(ldsdValue);
    assertThat(ldsdValue.getValue().doubleValue(), comparesEqualTo(0.0));
  }

  @Test
  public void computeLDSDAndGetItForTwoDifferentFormsOfWines_mustReturnValidResult() {
    Resource sauternesResource = new Resource(
        "http://www.w3.org/2002/07/owl#Class");
    Resource sweetRieslingResource = new Resource(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SweetRiesling");
    DecimalNormalizedAnalysisValue ldsdValue = ldsdMetricService
        .getValueFor(ResourcePair.of(sauternesResource, sweetRieslingResource));
    assertNotNull(ldsdValue);
    assertThat(ldsdValue.getValue().doubleValue(), closeTo(0.33, 0.34));
    DecimalNormalizedAnalysisValue ldsdValue2 = ldsdMetricService
        .getValueFor(ResourcePair.of(sweetRieslingResource, sauternesResource));
    assertThat(ldsdValue2.getValue().doubleValue(), closeTo(0.33, 0.34));
  }

  @Test
  public void computeLDSDAndGetItForTwoDistinctResources_mustReturnValidResult() {
    Resource sauternesResource = new Resource(
        "http://www.w3.org/2002/07/owl#Class");
    Resource sweetRieslingResource = new Resource(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling");
    DecimalNormalizedAnalysisValue ldsdValue = ldsdMetricService
        .getValueFor(ResourcePair.of(sauternesResource, sweetRieslingResource));
    assertNotNull(ldsdValue);
    assertThat(ldsdValue.getValue().doubleValue(), comparesEqualTo(1.0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void computeLDSDForNullPair_mustThrowIllegalArgumentException() {
    ldsdMetricService.getValueFor(null);
  }

  @Test
  public void computeLDSDForUnknownPair_mustReturnNull() {
    assertNull(ldsdMetricService
        .getValueFor(ResourcePair.of(new Resource("test://a"), new Resource("test://b"))));
  }
}
