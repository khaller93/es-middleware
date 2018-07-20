package at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.conf.IndexedMemoryKnowledgeGraphConfig;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.InformationContentService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.junit.Before;
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
 * This class implements integration test cases for {@link InformationContentServiceTest}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class, InformationContentService.class,
    IndexedMemoryKnowledgeGraph.class, InMemoryGremlinDAO.class, CentralityCacheManagerStub.class,
    KGDAOConfig.class, IndexedMemoryKnowledgeGraphConfig.class})
@TestPropertySource(properties = {"esm.db.choice=IndexedMemoryDB"})
public class InformationContentServiceTest {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  @Qualifier("getSparqlDAO")
  private KGSparqlDAO sparqlDAO;
  @Autowired
  @Qualifier("getGremlinDAO")
  private KGGremlinDAO gremlinDAO;
  @Autowired
  private GremlinService gremlinService;
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private CacheManager cacheManager;

  private InformationContentService informationContentService;

  @PostConstruct
  public void setUp() throws Exception {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Before
  public void before() throws InterruptedException {
    musicPintaResource.waitForAllDAOsBeingReady();
    informationContentService = new InformationContentService(gremlinService, applicationContext,
        cacheManager);
  }

  @Test
  public void test_getAllClasses_mustReturnInstrument() {
    List<Resource> allClasses = informationContentService.getAllClasses();
    assertThat(allClasses, not(hasSize(0)));
    assertThat(allClasses.stream().map(Resource::getId).collect(Collectors.toList()),
        hasItems("http://purl.org/ontology/mo/Instrument"));
  }

  @Test
  public void test_getInformationContent_mustReturnMap() {
    informationContentService.computeInformationContentForClasses();
    Double instrumentClassIC = informationContentService
        .getInformationContentOfClass(new Resource("http://purl.org/ontology/mo/Instrument"));
    assertNotNull(instrumentClassIC);
    assertThat(instrumentClassIC, greaterThan(0.0));
  }
}
