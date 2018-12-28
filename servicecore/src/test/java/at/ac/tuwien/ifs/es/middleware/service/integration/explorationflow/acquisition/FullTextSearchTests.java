package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.acquisition;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.payload.acquisition.FullTextSearchPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition.FullTextSearch;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.fts.SimpleFullTextSearchService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests the operator {@link FullTextSearch}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, FullTextSearch.class,
    SimpleFullTextSearchService.class, MusicPintaInstrumentsResource.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class FullTextSearchTests {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private FullTextSearch fullTextSearch;

  @Before
  public void setUp() throws InterruptedException {
    musicPintaResource.waitForAllDAOsBeingReady();
  }

  public List<Resource> mapToResource(List<String> iriStrings) {
    return iriStrings.stream().map(Resource::new).collect(Collectors.toList());
  }

  public List<Resource> mapToResource(String... iriStrings) {
    return Stream.of(iriStrings).map(Resource::new).collect(Collectors.toList());
  }

  @Test
  public void getGuitars_mustReturnContextWithCorrespondingResources() {
    ExplorationContext guitarContext = fullTextSearch
        .apply(null, new FullTextSearchPayload("guitar"));
    assertNotNull(guitarContext);
    assertThat(guitarContext, instanceOf(ResourceList.class));
    ResourceList resourceList = (ResourceList) guitarContext;
    assertThat(resourceList.asResourceSet(), hasSize(greaterThan(0)));
    assertThat(resourceList.asResourceSet(),
        hasItems(mapToResource("http://dbpedia.org/resource/Classical_guitar",
            "http://dbpedia.org/resource/Bass_guitar", "http://dbpedia.org/resource/Guitar",
            "http://dbpedia.org/resource/Fidola")
            .toArray(new Resource[0])));
    assertThat(resourceList.asResourceSet(),
        not(hasItem(new Resource("http://dbpedia.org/resource/Saxotromba"))));
  }

  @Test
  public void unknownKeyword_mustReturnEmptyContext() {
    ExplorationContext unknownKeywordContext = fullTextSearch
        .apply(null, new FullTextSearchPayload("TUWien"));
    assertNotNull(unknownKeywordContext);
    assertThat(unknownKeywordContext, instanceOf(ResourceList.class));
    ResourceList resourceList = (ResourceList) unknownKeywordContext;
    assertThat(resourceList.asResourceSet(), hasSize(0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void hasEmptyKeywordSpecified_mustThrowIllegalArgumentException() {
    fullTextSearch.apply(null, new FullTextSearchPayload(""));
  }

  @Test
  public void getGuitarsWhichAreMusicInstruments_mustReturnContextWithCorrespondingResources() {
    ExplorationContext guitarContext = fullTextSearch
        .apply(null, new FullTextSearchPayload("guitar",
            Collections.singletonList(new Resource("http://purl.org/ontology/mo/Instrument"))));
    assertNotNull(guitarContext);
    assertThat(guitarContext, instanceOf(ResourceList.class));
    ResourceList resourceList = (ResourceList) guitarContext;
    assertThat(resourceList.asResourceSet(), hasSize(greaterThan(0)));
    assertThat(resourceList.asResourceSet(),
        hasItems(mapToResource("http://dbpedia.org/resource/Classical_guitar",
            "http://dbpedia.org/resource/Bass_guitar", "http://dbpedia.org/resource/Guitar",
            "http://dbpedia.org/resource/Fidola")
            .toArray(new Resource[0])));
    assertThat(resourceList.asResourceSet(),
        not(hasItem(new Resource("http://dbtune.org/musicbrainz/resource/performance/27568"))));
  }

  @Test
  public void getFiveGuitars_mustReturnContextWithCorrespondingResources() {
    ExplorationContext guitarContext = fullTextSearch
        .apply(null, new FullTextSearchPayload("guitar",
            Collections.singletonList(new Resource("http://purl.org/ontology/mo/Instrument")), 0,
            5));
    assertNotNull(guitarContext);
    assertThat(guitarContext, instanceOf(ResourceList.class));
    ResourceList resourceList = (ResourceList) guitarContext;
    assertThat(resourceList.asResourceSet(), hasSize(5));
  }
}
