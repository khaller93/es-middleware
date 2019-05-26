package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ResourceClassService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ResourceClassWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.integration.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import at.ac.tuwien.ifs.es.middleware.testutil.util.TestUtil;
import java.util.Arrays;
import java.util.Optional;
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
 * This class tests {@link ResourceClassService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {KGDAOConfig.class, RDF4JDAOConfig.class,
    ClonedInMemoryGremlinDAO.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    RDF4JLuceneFullTextSearchDAO.class, ThreadPoolConfig.class,
    SimpleSPARQLService.class, MapDBDummy.class, SpringCacheConfig.class,
    WineOntologyDatasetResource.class, AllResourcesWithSPARQLService.class,
    ResourceClassWithSPARQLService.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class ResourceClassServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private AllResourcesService allResourcesService;
  @Autowired
  private ResourceClassService resourceClassService;

  @Before
  public void setUp() throws Exception {
    allResourcesService.compute();
    resourceClassService.compute();
  }

  @Test
  public void getClassesOfSpecialWineResource_mustReturnAllClasses() {
    Optional<Set<Resource>> classesOptional = resourceClassService
        .getClassesOf(new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne"));
    assertTrue(classesOptional.isPresent());
    assertThat(classesOptional.get(),
        containsInAnyOrder(TestUtil.mapToResource(Arrays.asList(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#LateHarvest",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Bordeaux",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#PotableLiquid",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getClassesOfNull_mustThrowIllegalArgumentException() {
    resourceClassService.getClassesOf(null);
  }

  @Test
  public void getClassesOfUnknownResource_mustReturnEmptyOptional() {
    Optional<Set<Resource>> classesOptional = resourceClassService
        .getClassesOf(new Resource("test://a"));
    assertFalse(classesOptional.isPresent());
  }
}
