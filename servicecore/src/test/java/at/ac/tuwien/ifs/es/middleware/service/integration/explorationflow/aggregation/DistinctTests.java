package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.VoidPayload;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.SameAsResourceWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation.Distinct;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation.Limit;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests the operator {@link Limit}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, Distinct.class,
    SameAsResourceWithSPARQLService.class, SpringCacheConfig.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class DistinctTests {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private KGSparqlDAO sparqlDAO;
  @Autowired
  private KGGremlinDAO gremlinDAO;
  @Autowired
  private SameAsResourceService sameAsResourceService;
  @Autowired
  private Distinct distinct;

  private List<Resource> resourceList;
  private ResourceList resourceListContext;

  @PostConstruct
  public void setUpInstance() {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Before
  public void setUp() throws Exception {
    resourceList = Lists
        .newArrayList(new Resource("http://dbpedia.org/resource/Violin"),
            new Resource("http://dbtune.org/musicbrainz/resource/instrument/229"),
            new Resource("http://dbpedia.org/resource/Guitar"),
            new Resource("http://dbpedia.org/resource/Banjo"),
            new Resource("http://dbpedia.org/resource/Banjo_uke"),
            new Resource("http://dbtune.org/musicbrainz/resource/instrument/92"));
    resourceListContext = new ResourceList(resourceList);
    musicPintaResource.waitForAllDAOsBeingReady();
    sameAsResourceService.compute();
  }

  public List<Resource> mapToResource(List<String> iriStrings) {
    return iriStrings.stream().map(Resource::new).collect(Collectors.toList());
  }

  public List<Resource> mapToResource(String... iriStrings) {
    return Stream.of(iriStrings).map(Resource::new).collect(Collectors.toList());
  }

  @Test
  public void distinctEmptyContext_mustReturnEmptyContext() {
    ExplorationContext context = distinct.apply(new ResourceList(), new VoidPayload());
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceList(), hasSize(0));
  }

  @Test
  public void distinctContext_mustReturnContextWithOnlyUniqueDistinctResources() {
    ExplorationContext context = distinct.apply(resourceListContext, new VoidPayload());
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceListContextResponse = (ResourceList) context;
    assertThat(resourceListContextResponse.asResourceSet(), hasSize(3));
    assertThat(resourceListContextResponse.asResourceSet(),
        hasItems(mapToResource("http://dbpedia.org/resource/Violin",
            "http://dbtune.org/musicbrainz/resource/instrument/229",
            "http://dbpedia.org/resource/Banjo").toArray(new Resource[0])));
  }
}