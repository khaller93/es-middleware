package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis;


import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAODependencyGraphService;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAOScheduler;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.PrimaryKGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.scheduler.SchedulerPipeline;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.ClassHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.general.ResourceHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.general.ResourceHierarchyServiceImpl;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests {@link ClassHierarchyService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class, RDF4JLuceneFullTextSearchDAO.class,
    SimpleSPARQLService.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    ClonedInMemoryGremlinDAO.class, ThreadPoolConfig.class, PrimaryKGDAOConfig.class,
    RDF4JDAOConfig.class, ThreadPoolConfig.class, SpringCacheConfig.class, MapDBDummy.class,
    AllResourcesWithSPARQLService.class, DAOScheduler.class, SchedulerPipeline.class,
    MapDBDummy.class, DAODependencyGraphService.class, WineOntologyDatasetResource.class,
    ResourceHierarchyServiceImpl.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ResourceHierarchyTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private ResourceHierarchyService resourceHierarchyService;

  @Before
  public void setUp() throws Exception {
    resourceHierarchyService.compute();
  }

  @Test
  public void name() {
    System.out.println(resourceHierarchyService
        .getHierarchy(Sets.newHashSet(new Resource("http://www.w3.org/2002/07/owl#Class")),
            Sets.newHashSet(), Sets.newHashSet(),
            Sets.newHashSet(new Resource("http://www.w3.org/2000/01/rdf-schema#subClassOf"))));
  }
}
