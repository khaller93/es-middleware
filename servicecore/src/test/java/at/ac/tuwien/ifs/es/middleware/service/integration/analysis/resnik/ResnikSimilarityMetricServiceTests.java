package at.ac.tuwien.ifs.es.middleware.service.integration.analysis.resnik;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.lca.LowestCommonAncestorService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik.ResnikSimilarityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.resnik.ResnikSimilarityMetricServiceImpl;
import at.ac.tuwien.ifs.es.middleware.service.integration.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
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
 * This class tests {@link ResnikSimilarityMetricService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class ResnikSimilarityMetricServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private ResnikSimilarityMetricService resnikSimilarityMetricService;
  @Autowired
  private ClassEntropyService classEntropyService;

  public void setUp() throws Exception {
    classEntropyService.compute();
    resnikSimilarityMetricService.compute();
  }

  @Test(expected = IllegalArgumentException.class)
  public void computeResnikSimForNullPair_mustThrowIllegalArgumentException() {
    resnikSimilarityMetricService.getValueFor(null);
  }

  @Test
  public void computeResnikSimForSpecificWineResourcePair_mustReturnValue() {
    Resource guitarResource = new Resource(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne");
    Resource spanishAcousticGuitarResource = new Resource(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera");
    Double resnikValue = resnikSimilarityMetricService
        .getValueFor(ResourcePair.of(guitarResource, spanishAcousticGuitarResource));
    assertNotNull(resnikValue);
    assertThat(resnikValue, greaterThan(0.0));
    assertThat(resnikValue, is(classEntropyService.getEntropyForClass(
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"))));
  }

  @Test
  public void computeResnikSimForUnknownPair_mustBeZeroValue() {
    Double resnikValue = resnikSimilarityMetricService
        .getValueFor(ResourcePair.of(new Resource("test://a"), new Resource("test://b")));
    assertThat(resnikValue, is(0.0));
  }
}
