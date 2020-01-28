package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAODependencyGraphService;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAOScheduler;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.PrimaryKGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.scheduler.SchedulerPipeline;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
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
 * This class tests {@link SameAsResourceService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, SimpleGremlinService.class,
    RDF4JLuceneFullTextSearchDAO.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    ClonedInMemoryGremlinDAO.class, ThreadPoolConfig.class, PrimaryKGDAOConfig.class, RDF4JDAOConfig.class,
    ThreadPoolConfig.class, MapDBDummy.class, MusicPintaInstrumentsResource.class,
    SameAsResourceWithSPARQLService.class, AllResourcesWithSPARQLService.class,
    DAOScheduler.class, SchedulerPipeline.class, MapDBDummy.class, DAODependencyGraphService.class,
})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class SameAsResourceServiceTests {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private AllResourcesService allResourcesService;
  @Autowired
  private SameAsResourceService sameAsResourceService;

  @Before
  public void setUp() throws Exception {
    allResourcesService.compute();
    sameAsResourceService.compute();
  }

  @Test
  public void computeTheSameAsResources_mustReturnMapForAllKnownResources() {
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

  @Test(expected = IllegalArgumentException.class)
  public void computeTheSameAsForNull_mustThrowIllegalArgumentException(){
    sameAsResourceService.getSameAsResourcesFor(null);
  }

}
