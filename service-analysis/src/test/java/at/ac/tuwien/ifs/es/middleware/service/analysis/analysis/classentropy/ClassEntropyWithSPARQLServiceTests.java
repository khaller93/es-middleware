package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.classentropy;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAODependencyGraphService;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAOScheduler;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.PrimaryKGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.scheduler.SchedulerPipeline;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.testutil.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests {@link ClassEntropyWithSPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class, RDF4JLuceneFullTextSearchDAO.class,
    SimpleSPARQLService.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    ClonedInMemoryGremlinDAO.class, ThreadPoolConfig.class, PrimaryKGDAOConfig.class, RDF4JDAOConfig.class,
    ThreadPoolConfig.class, AllClassesWithSPARQLService.class, SpringCacheConfig.class,
    AllResourcesWithSPARQLService.class, MapDBDummy.class, WineOntologyDatasetResource.class,
    DAOScheduler.class, SchedulerPipeline.class, MapDBDummy.class, DAODependencyGraphService.class,
    ClassEntropyWithSPARQLService.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
})
public class ClassEntropyWithSPARQLServiceTests extends ClassEntropyServiceTests {

  @Autowired
  private AllResourcesService allResourcesService;
  @Autowired
  private AllClassesService allClassesService;

  @Override
  @Before
  public void setUp() throws Exception {
    allResourcesService.compute();
    allClassesService.compute();
    super.setUp();
  }
}
