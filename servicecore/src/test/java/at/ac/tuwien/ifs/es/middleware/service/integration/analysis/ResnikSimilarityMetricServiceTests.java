package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassEntropyWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassInformationServiceImpl;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LCSWithInMemoryTreeService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LeastCommonSubSumersService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.SameAsResourceWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik.ResnikSimilarityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik.ResnikSimilarityMetricServiceImpl;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests {@link ResnikSimilarityMetricService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, SimpleGremlinService.class,
    RDF4JLuceneFullTextSearchDAO.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    ClonedInMemoryGremlinDAO.class, ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class,
    ThreadPoolConfig.class, ClassInformationServiceImpl.class, SpringCacheConfig.class,
    SameAsResourceWithSPARQLService.class, AnalysisPipelineProcessorDummy.class,
    MusicPintaInstrumentsResource.class, ResnikSimilarityMetricServiceImpl.class,
    LCSWithInMemoryTreeService.class, ClassEntropyWithGremlinService.class,
    SameAsResourceWithSPARQLService.class
})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
@DataJpaTest(showSql = false)
@Ignore("Takes too long for current test data")
public class ResnikSimilarityMetricServiceTests {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private SameAsResourceService sameAsResourceService;
  @Autowired
  private ClassEntropyService classEntropyService;
  @Autowired
  private LeastCommonSubSumersService leastCommonSubSumersService;
  @Autowired
  private ResnikSimilarityMetricService resnikSimilarityMetricService;

  @Before
  public void setUp() throws InterruptedException {
    musicPintaResource.waitForAllDAOsBeingReady();
    sameAsResourceService.compute();
    classEntropyService.compute();
    leastCommonSubSumersService.compute();
  }

  @Test
  public void computeResnikSimAndAskForResourcePair_mustReturnValue() {
    Resource guitarResource = new Resource("http://dbpedia.org/resource/Guitar");
    Resource spanishAcousticGuitarResource = new Resource(
        "http://dbtune.org/musicbrainz/resource/instrument/206");
    resnikSimilarityMetricService.compute();
    Double resnikValue = resnikSimilarityMetricService
        .getValueFor(ResourcePair.of(guitarResource, spanishAcousticGuitarResource));
    assertNotNull(resnikValue);
    assertThat(resnikValue, greaterThan(0.0));
  }
}
