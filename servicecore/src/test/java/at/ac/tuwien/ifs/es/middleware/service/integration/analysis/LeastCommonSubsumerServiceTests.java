package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;


import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.LCSWithInMemoryTreeService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.LeastCommonSubsumersService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class should test the {@link LeastCommonSubsumersService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, ThreadPoolConfig.class,
    SameAsResourceWithSPARQLService.class, SpringCacheConfig.class,
    AllClassesWithSPARQLService.class, AnalysisPipelineProcessorDummy.class,
    MusicPintaInstrumentsResource.class, LCSWithInMemoryTreeService.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
})
public class LeastCommonSubsumerServiceTests {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private LeastCommonSubsumersService leastCommonSubSumersService;


  @Before
  public void setUp() throws Exception {
    musicPintaResource.waitForAllDAOsBeingReady();
  }

  @Test
  public void leastcommonSubsummersOfTwoGuitarInstruments_mustReturnGuitarClass() {
    Resource guitarResource = new Resource("http://dbpedia.org/resource/Guitar");
    Resource spanishAcousticGuitarResource = new Resource(
        "http://dbtune.org/musicbrainz/resource/instrument/206");
    leastCommonSubSumersService.compute();
    Set<Resource> leastCommonSubSummers = leastCommonSubSumersService
        .getLeastCommonSubsumersFor(
            ResourcePair.of(guitarResource, spanishAcousticGuitarResource));
    assertNotNull(leastCommonSubSummers);
    assertThat(leastCommonSubSummers,
        hasItem(new Resource("http://purl.org/ontology/mo/Instrument")));
  }

  @Test
  public void leastCommonSubsummerOfTwoUnrelatedResources_mustReturnEmptyList() {
    Resource guitarResource = new Resource("http://dbpedia.org/resource/Guitar");
    Resource testAResource = new Resource("test_a");
    leastCommonSubSumersService.compute();
    Set<Resource> leastCommonSubSummers = leastCommonSubSumersService
        .getLeastCommonSubsumersFor(ResourcePair.of(guitarResource, testAResource));
    assertNotNull(leastCommonSubSummers);
    assertThat(leastCommonSubSummers, hasSize(0));
  }
}
