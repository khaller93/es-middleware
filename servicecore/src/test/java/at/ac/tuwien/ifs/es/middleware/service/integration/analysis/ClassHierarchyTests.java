package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.ClassHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.ClassHierarchyWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ResourceClassService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition.AllResources;
import at.ac.tuwien.ifs.es.middleware.service.integration.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import at.ac.tuwien.ifs.es.middleware.testutil.util.TestUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests {@link at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.ClassHierarchyService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class, RDF4JLuceneFullTextSearchDAO.class,
    SimpleSPARQLService.class, RDF4JMemoryStoreWithLuceneSparqlDAO.class,
    ClonedInMemoryGremlinDAO.class, ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class,
    ThreadPoolConfig.class, AllClassesWithSPARQLService.class, SpringCacheConfig.class,
    SameAsResourceWithSPARQLService.class, MapDBDummy.class, AllResourcesWithSPARQLService.class,
    WineOntologyDatasetResource.class, ClassHierarchyWithSPARQLService.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
})
public class ClassHierarchyTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private AllResourcesService allResourcesService;
  @Autowired
  private AllClassesService allClassesService;
  @Autowired
  private SameAsResourceService sameAsResourceService;
  @Autowired
  private ClassHierarchyService classHierarchyService;

  @Before
  public void setUp() throws Exception {
    allResourcesService.compute();
    sameAsResourceService.compute();
    allClassesService.compute();
    classHierarchyService.compute();
  }

  @Test
  public void getAllClassesForTwoClasses_mustReturnGivenClassesPlusParentClasses() {
    Set<Resource> allClasses = classHierarchyService
        .getAllClasses(Sets.newLinkedHashSet(TestUtil.mapToResource(
            Arrays.asList("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SweetRiesling"))));
    assertNotNull(allClasses);
    assertFalse(allClasses.isEmpty());
    assertThat(allClasses, containsInAnyOrder(TestUtil
        .mapToResource(
            Arrays.asList("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Bordeaux",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#PotableLiquid",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#LateHarvest",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#DessertWine",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SweetRiesling"))));
  }

  @Test
  public void getAllClassesEmpty_mustReturnEmpty() {
    Set<Resource> allClasses = classHierarchyService.getAllClasses(Sets.newHashSet());
    assertNotNull(allClasses);
    assertTrue(allClasses.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getAllClassesForNull_mustThrowIllegalArgumentException() {
    classHierarchyService.getAllClasses(null);
  }

  @Test
  public void getMostSpecificClass_mustReturnOnlySpecificWineClass() {
    Set<Resource> mostSpecificClasses = classHierarchyService
        .getMostSpecificClasses(Sets.newLinkedHashSet(TestUtil
            .mapToResource(
                Arrays.asList("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#DessertWine",
                    "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SweetRiesling",
                    "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"))));
    assertNotNull(mostSpecificClasses);
    assertFalse(mostSpecificClasses.isEmpty());
    assertThat(mostSpecificClasses, containsInAnyOrder(TestUtil.mapToResource(
        Collections
            .singletonList("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SweetRiesling"))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getMostSpecificClassOfNull_throwsIllegalArgumentException() {
    classHierarchyService.getMostSpecificClasses(null);
  }

  @Test
  public void getMostSpecificClassOfEmptyList_mustReturnEmptyList() {
    Set<Resource> mostSpecificClasses = classHierarchyService
        .getMostSpecificClasses(Collections.emptySet());
    assertNotNull(mostSpecificClasses);
    assertTrue(mostSpecificClasses.isEmpty());
  }

  @Test
  public void getParentClassesFor_mustReturnAllParentClasses() {
    Set<Resource> parentClasses = classHierarchyService.getParentClasses(
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes"));
    assertNotNull(parentClasses);
    assertFalse(parentClasses.isEmpty());
    assertThat(parentClasses, containsInAnyOrder(TestUtil.mapToResource(
        Arrays.asList("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Bordeaux",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/food#PotableLiquid",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#LateHarvest"))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getParentClassForNull_mustThrowIllegalArgumentException() {
    classHierarchyService.getParentClasses(null);
  }

  @Test
  public void getLCAOfSweetRieslingAndSauternes_mustReturnWineClass() {
    Set<Resource> lowestCommonAncestor = classHierarchyService.getLowestCommonAncestor(
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SweetRiesling"),
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Sauternes"));
    assertNotNull(lowestCommonAncestor);
    assertFalse(lowestCommonAncestor.isEmpty());
    assertThat(lowestCommonAncestor, containsInAnyOrder(TestUtil.mapToResource(
        Collections.singletonList("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"))));
  }

}
