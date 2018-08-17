package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassEntropyWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassInformationService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.ClassInformationServiceImpl;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LCSWithInMemoryTreeService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.LeastCommonSubSumersService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.SameAsResourceWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik.ResnikSimilarityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik.ResnikSimilarityMetricServiceImpl;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import javax.annotation.PostConstruct;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
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
    SameAsResourceWithSPARQLService.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class ResnikSimilarityMetricServiceTests {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  public KGSparqlDAO sparqlDAO;
  @Autowired
  private KGGremlinDAO gremlinDAO;
  @Autowired
  private TaskExecutor taskExecutor;
  @Autowired
  private SPARQLService sparqlService;
  @Autowired
  private GremlinService gremlinService;
  @Autowired
  private ApplicationContext context;
  @Autowired
  private CacheManager cacheManager;
  @Autowired
  private ClassInformationService classInformationService;

  private PGS schema;
  private ResnikSimilarityMetricService resnikSimilarityMetricService;

  @PostConstruct
  public void setUpInstance() {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Before
  public void setUp() throws InterruptedException {
    musicPintaResource.waitForAllDAOsBeingReady();
    SameAsResourceService sameAsResourceService = new SameAsResourceWithSPARQLService(sparqlService,
        context, taskExecutor, cacheManager);
    sameAsResourceService.compute();
    ClassEntropyService classEntropyService = new ClassEntropyWithGremlinService(gremlinService,
        classInformationService, context, taskExecutor);
    classEntropyService.compute();
    LeastCommonSubSumersService leastCommonSubSumersService = new LCSWithInMemoryTreeService(
        sparqlService, classInformationService, sameAsResourceService, context, taskExecutor,
        cacheManager);
    leastCommonSubSumersService.compute();
    resnikSimilarityMetricService = new ResnikSimilarityMetricServiceImpl(sparqlService,
        classEntropyService, leastCommonSubSumersService, cacheManager);
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
