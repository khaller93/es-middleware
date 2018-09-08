package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.SameAsResourceWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests {@link SameAsResourceService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, SimpleGremlinService.class,
    RDF4JLuceneFullTextSearchDAO.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    ClonedInMemoryGremlinDAO.class, ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class,
    ThreadPoolConfig.class, SpringCacheConfig.class, AnalysisPipelineProcessorDummy.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class SameAsResourceServiceTests {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private KGSparqlDAO sparqlDAO;
  @Autowired
  private KGGremlinDAO gremlinDAO;
  @Autowired
  private SPARQLService sparqlService;
  @Autowired
  private AnalysisPipelineProcessor processor;
  @Autowired
  private CacheManager cacheManager;

  private SameAsResourceService sameAsResourceService;

  @PostConstruct
  public void setUpInstance() {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Before
  public void setUp() throws InterruptedException {
    musicPintaResource.waitForAllDAOsBeingReady();
    sameAsResourceService = new SameAsResourceWithSPARQLService(sparqlService, processor,
        cacheManager);
  }

  @Test
  public void computeTheSameAsResources_mustReturnMapForAllKnownResources() {
    sameAsResourceService.compute();
    Set<Resource> sameAsResources = sameAsResourceService
        .getSameAsResourcesFor(new Resource("http://dbpedia.org/resource/Guitar"));
    assertNotNull(sameAsResources);
    assertThat(sameAsResources, hasSize(1));
    assertThat(sameAsResources,
        hasItem(new Resource("http://dbtune.org/musicbrainz/resource/instrument/229")));
  }

  @Test
  public void computeTheSameAsResourcesAndGetItForUnknownResources_mustReturnNull() {
    Set<Resource> sameAsResourcesSet = sameAsResourceService
        .getSameAsResourcesFor(new Resource("test:a"));
    assertNotNull(sameAsResourcesSet);
    assertThat(sameAsResourcesSet, hasSize(0));
  }
}
