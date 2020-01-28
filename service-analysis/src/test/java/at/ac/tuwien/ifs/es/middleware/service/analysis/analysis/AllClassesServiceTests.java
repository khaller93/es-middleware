package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import at.ac.tuwien.ifs.es.middleware.testutil.util.TestUtil;
import java.util.Arrays;
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
 * This class should test the {@link AllClassesService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, PrimaryKGDAOConfig.class, RDF4JDAOConfig.class, ThreadPoolConfig.class,
    AllClassesWithSPARQLService.class, WineOntologyDatasetResource.class,
    DAOScheduler.class, SchedulerPipeline.class, MapDBDummy.class, DAODependencyGraphService.class,
    MapDBDummy.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class AllClassesServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private AllClassesService allClassesService;

  @Before
  public void setUp() throws Exception {
    allClassesService.compute();
  }

  @Test
  public void test_getAllClasses_mustBeSuccessful() {
    Set<Resource> allClasses = allClassesService.getAllClasses();
    assertNotNull(allClasses);
    assertFalse(allClasses.isEmpty());
    assertThat(allClasses, hasItems(TestUtil.mapToResource(Arrays
        .asList("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WineDescriptor",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WineColor",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WineSugar",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WineTaste",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WineGrape",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WineBody",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WineFlavor",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#CabernetSauvignon"))));
    assertThat(allClasses, everyItem(
        not(isIn(TestUtil.mapToResource(Arrays.asList("http://www.w3.org/2000/01/rdf-schema#label",
            "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SauvignonBlancGrape",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#White"))))));
  }

}
