package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.CachingConfig;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.peerpressure.PeerPressureClusteringMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.peerpressure.PeerPressureClusteringMetricWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import javax.annotation.PostConstruct;
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
 * This class tests {@link PeerPressureClusteringMetricService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, SimpleGremlinService.class,
    RDF4JLuceneFullTextSearchDAO.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    ClonedInMemoryGremlinDAO.class, ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class,
    ThreadPoolConfig.class, CachingConfig.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class PeerPressureClusteringMetricServiceTests {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private KGSparqlDAO sparqlDAO;
  @Autowired
  private KGGremlinDAO gremlinDAO;
  @Autowired
  private GremlinService gremlinService;
  @Autowired
  private ApplicationContext context;
  @Autowired
  private TaskExecutor taskExecutor;

  private PeerPressureClusteringMetricService peerPressureClusteringMetricService;

  @PostConstruct
  public void setUpBean() {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
    peerPressureClusteringMetricService = new PeerPressureClusteringMetricWithGremlinService(
        gremlinService, context, taskExecutor);
  }

  @Before
  public void setUp() throws Exception {
    musicPintaResource.waitForAllDAOsBeingReady();
  }

  @Test
  public void computeThePeerPressureAndGetItForSameResourcesPair_mustReturnTrue() {
    peerPressureClusteringMetricService.compute();
    ResourcePair resourcePair = ResourcePair.of(new Resource("http://dbpedia.org/resource/Guitar"),
        new Resource("http://dbpedia.org/resource/Guitar"));
    Boolean result = peerPressureClusteringMetricService.isSharingSameCluster(resourcePair);
    assertNotNull(result);
    assertTrue(result);
  }

  @Test
  public void computeThePeerPressureAndGetItForTwoResourcesInDifferentCLuster_mustReturnFalse() {
    peerPressureClusteringMetricService.compute();
    ResourcePair resourcePair = ResourcePair.of(new Resource("http://dbpedia.org/resource/Guitar"),
        new Resource("http://purl.org/ontology/mo/Performance"));
    Boolean result = peerPressureClusteringMetricService.isSharingSameCluster(resourcePair);
    assertNotNull(result);
    assertFalse(result);
  }

  @Test
  public void computeThePeerPressureAndGetItForUnknownPair_mustReturnNull() {
    peerPressureClusteringMetricService.compute();
    ResourcePair resourcePair = ResourcePair.of(new Resource("test:a"),
        new Resource("http://dbtune.org/musicbrainz/resource/instrument/233"));
    Boolean result = peerPressureClusteringMetricService.isSharingSameCluster(resourcePair);
    assertNull(result);
  }

  @Test
  public void computeThePeerPressureAndGetItForUnknownPair2_mustReturnNull() {
    peerPressureClusteringMetricService.compute();
    ResourcePair resourcePair = ResourcePair.of(new Resource("http://dbpedia.org/resource/Guitar"),
        new Resource("test:a"));
    Boolean result = peerPressureClusteringMetricService.isSharingSameCluster(resourcePair);
    assertNull(result);
  }

  @Test
  public void computeThePeerPressureAndGetItForUnknownPair3_mustReturnNull() {
    peerPressureClusteringMetricService.compute();
    ResourcePair resourcePair = ResourcePair.of(new Resource("test:b"),
        new Resource("test:a"));
    Boolean result = peerPressureClusteringMetricService.isSharingSameCluster(resourcePair);
    assertNull(result);
  }
}
