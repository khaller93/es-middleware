package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
import at.ac.tuwien.ifs.es.middleware.service.analysis.JPAConfiguration;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.SimilarityMetricKey;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.SimilarityMetricResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.SimilarityMetricStoreService;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd.LDSDWithSPARQLMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd.LinkedDataSemanticDistanceMetricService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import javax.annotation.PostConstruct;
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
 * This class tests {@link LDSDWithSPARQLMetricService} that is unweighted.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {KGDAOConfig.class, RDF4JDAOConfig.class,
    ClonedInMemoryGremlinDAO.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    RDF4JLuceneFullTextSearchDAO.class, ThreadPoolConfig.class, LDSDWithSPARQLMetricService.class,
    SimpleSPARQLService.class, AnalysisPipelineProcessorDummy.class, SpringCacheConfig.class,
    JPAConfiguration.class, SimilarityMetricStoreService.class, SimilarityMetricResult.class,
    SimilarityMetricKey.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
})
@DataJpaTest(showSql = false)
@Ignore("Takes too long for current test data")
public class UnweightedLDSDMetricServiceTests {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  public KGSparqlDAO sparqlDAO;
  @Autowired
  private KGGremlinDAO gremlinDAO;
  @Autowired
  private LinkedDataSemanticDistanceMetricService ldsdMetricService;

  @PostConstruct
  public void setUpInstance() {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Before
  public void setUp() throws InterruptedException {
    musicPintaResource.waitForAllDAOsBeingReady();
  }

  @Test
  public void computeLDSDAndGetItForTwoDifferentFormsOfGuitars_mustReturnValidResult() {
    ldsdMetricService.compute();
    Resource guitarResource = new Resource("http://dbpedia.org/resource/Guitar");
    Resource spanishAcousticGuitarResource = new Resource(
        "http://dbtune.org/musicbrainz/resource/instrument/206");
    Double ldsdValue = ldsdMetricService
        .getValueFor(ResourcePair.of(guitarResource, spanishAcousticGuitarResource));
    assertNotNull(ldsdValue);
    assertThat(ldsdValue, greaterThan(0.0));
    assertThat(ldsdValue, lessThan(1.0));
  }

  @Test
  public void getLDSDForTwoDifferentFormsOfGuitarsOnline_mustReturnValidResult() {
    ldsdMetricService.compute();
    Resource guitarResource = new Resource("http://dbpedia.org/resource/Guitar");
    Resource spanishAcousticGuitarResource = new Resource(
        "http://dbtune.org/musicbrainz/resource/instrument/206");
    Double ldsdValue = ldsdMetricService
        .getValueFor(ResourcePair.of(guitarResource, spanishAcousticGuitarResource));
    assertNotNull(ldsdValue);
    assertThat(ldsdValue, greaterThan(0.0));
    assertThat(ldsdValue, lessThan(1.0));
  }
}
