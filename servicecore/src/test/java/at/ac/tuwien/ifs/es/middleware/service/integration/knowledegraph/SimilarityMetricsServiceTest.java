package at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.conf.IndexedMemoryKnowledgeGraphConfig;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.CentralityMetricsService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.InformationContentService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimilarityMetricsService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class unit tests {@link SimilarityMetricsService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class, IndexedMemoryKnowledgeGraph.class,
    InMemoryGremlinDAO.class, SimpleSPARQLService.class, CentralityCacheManagerStub.class,
    KGDAOConfig.class, IndexedMemoryKnowledgeGraphConfig.class})
@TestPropertySource(properties = {"esm.db.choice=IndexedMemoryDB"})
public class SimilarityMetricsServiceTest {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  @Qualifier("getSparqlDAO")
  private KGSparqlDAO sparqlDAO;
  @Autowired
  @Qualifier("getGremlinDAO")
  private KGGremlinDAO gremlinDAO;
  @Autowired
  private SPARQLService sparqlService;
  @Autowired
  private GremlinService gremlinService;
  @Autowired
  private CacheManager cacheManager;
  @Autowired
  private ApplicationContext applicationContext;

  private static final Resource vielle = new Resource(
      BlankOrIRIJsonUtil.valueOf("http://dbtune.org/musicbrainz/resource/instrument/116"));
  private static final Resource violin = new Resource(
      BlankOrIRIJsonUtil.valueOf("http://dbtune.org/musicbrainz/resource/instrument/86"));
  private static List<ResourcePair> resourcePairList;

  private CentralityMetricsService centralityMetricsService;
  private InformationContentService informationContentService;
  private SimilarityMetricsService similarityMetricsService;

  @BeforeClass
  public static void setUpClass() throws Exception {
    resourcePairList = Arrays
        .asList(ResourcePair.of(vielle, vielle), ResourcePair.of(violin, vielle),
            ResourcePair.of(vielle, violin));
  }

  @PostConstruct
  public void setUp() throws Exception {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Before
  public void before() throws InterruptedException {
    musicPintaResource.waitForAllDAOsBeingReady();
    centralityMetricsService = new CentralityMetricsService(gremlinService, cacheManager,
        applicationContext);
    informationContentService = new InformationContentService(gremlinService, applicationContext,
        cacheManager);
    similarityMetricsService = new SimilarityMetricsService(sparqlService, gremlinService,
        centralityMetricsService, informationContentService, cacheManager);
  }

  @Test
  @Ignore
  public void tests_getDistanceBetweenPairs() {
    similarityMetricsService.computeDistance();
    Map<ResourcePair, Integer> distanceMap = similarityMetricsService.getDistance(resourcePairList);
    assertThat("The distance between same resource must be 0.",
        distanceMap.get(ResourcePair.of(vielle, vielle)), is(equalTo(0)));
    assertThat("The distance between vielle and violin must be 1, due to subclass relationship.",
        distanceMap.get(ResourcePair.of(vielle, violin)), is(equalTo(1)));
    assertThat("The distance between resources is symmetric.",
        distanceMap.get(ResourcePair.of(vielle, violin)),
        is(equalTo(distanceMap.get(ResourcePair.of(violin, vielle)))));
  }
}
