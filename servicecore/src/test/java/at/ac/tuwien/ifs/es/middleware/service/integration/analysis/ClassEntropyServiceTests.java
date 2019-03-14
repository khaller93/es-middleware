package at.ac.tuwien.ifs.es.middleware.service.integration.analysis;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.integration.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests {@link ClassEntropyService}.
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
    MapDBDummy.class, WineOntologyDatasetResource.class, ClassEntropyWithSPARQLService.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin",
})
public class ClassEntropyServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private ClassEntropyService classEntropyService;
  @Autowired
  private AllClassesService allClassesService;

  @Before
  public void setUp() throws Exception {
    allClassesService.compute();
    classEntropyService.compute();
  }

  @Test
  public void getEntropyForWine_mustReturnNonZeroValue() {
    Double wineEntropy = classEntropyService
        .getEntropyForClass(
            new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"));
    assertNotNull(wineEntropy);
    assertThat(wineEntropy, greaterThan(0.0));
    Double italianWineEntropy = classEntropyService
        .getEntropyForClass(
            new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ItalianWine"));
    assertNotNull(italianWineEntropy);
    assertThat(italianWineEntropy, greaterThan(0.0));
    assertThat(wineEntropy, lessThan(italianWineEntropy));
  }

  @Test
  public void getEntropyForNonClass_mustBeNull() {
    Double categoryEntropy = classEntropyService
        .getEntropyForClass(
            new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Bancroft"));
    assertNull(categoryEntropy);
  }

  @Test
  public void getEntropyForUnknownResource_mustBeNull() {
    Double unknownResourceEntropy = classEntropyService
        .getEntropyForClass(new Resource("test:a"));
    assertNull(unknownResourceEntropy);
  }
}
