package at.ac.tuwien.ifs.es.middleware.service.exploration.explorationflow.acquisition;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAODependencyGraphService;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DAOScheduler;
import at.ac.tuwien.ifs.es.middleware.scheduler.SchedulerPipeline;
import at.ac.tuwien.ifs.es.middleware.service.caching.SpringCacheConfig;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.resources.ResourceList;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.PrimaryKGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.ClassHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.ClassHierarchyWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ClassResourceService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ClassResourceWithGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.SameAsResourceWithSPARQLService;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition.AllResources;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.AllResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MapDBDummy;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests the operator {@link AllResources}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, PrimaryKGDAOConfig.class, RDF4JDAOConfig.class, AllResources.class,
    MusicPintaInstrumentsResource.class, AllResourcesWithSPARQLService.class, MapDBDummy.class,
    ClassResourceWithGremlinService.class, ClassHierarchyWithSPARQLService.class,
    SameAsResourceWithSPARQLService.class, AllClassesWithSPARQLService.class,
    DAOScheduler.class, SchedulerPipeline.class, DAODependencyGraphService.class,
    SimpleGremlinService.class, SpringCacheConfig.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class AllResourcesTests {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private AllResourcesService allResourcesService;
  @Autowired
  private SameAsResourceService sameAsResourceService;
  @Autowired
  private AllClassesService allClassesService;
  @Autowired
  private ClassHierarchyService classHierarchyService;
  @Autowired
  private AllResources allResources;
  @Autowired
  private ClassResourceService classResourceService;

  @Before
  public void setUp() throws InterruptedException {
    allResourcesService.compute();
    allClassesService.compute();
    sameAsResourceService.compute();
    classHierarchyService.compute();
    classResourceService.compute();
  }

  public List<Resource> mapToResource(List<String> iriStrings) {
    return iriStrings.stream().map(Resource::new).collect(Collectors.toList());
  }

  public List<Resource> mapToResource(String... iriStrings) {
    return Stream.of(iriStrings).map(Resource::new).collect(Collectors.toList());
  }

  @Test
  public void getAllResources_mustReturnContextWithAll() {
    ExplorationContext context = allResources
        .apply(null, new AllResourcesPayload(null, null, null, null));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceList = (ResourceList) context;
    Set<Resource> allResourceSet = resourceList.asResourceSet();
    assertThat(allResourceSet, hasSize(greaterThan(0)));
    assertThat(allResourceSet,
        hasItems(mapToResource("http://dbtune.org/musicbrainz/resource/instrument/116",
            "http://dbtune.org/musicbrainz/resource/instrument/104",
            "http://dbpedia.org/resource/Dhol",
            "http://dbtune.org/musicbrainz/resource/instrument/170",
            "http://dbtune.org/musicbrainz/resource/instrument/201",
            "http://dbpedia.org/resource/Guitarra_de_golpe",
            "http://dbtune.org/musicbrainz/resource/performance/98375",
            "http://dbtune.org/musicbrainz/resource/performance/6870").toArray(new Resource[0])));
  }

  @Test
  public void getAllResourcesEmptyLists_mustReturnContextWithAll() {
    ExplorationContext context = allResources
        .apply(null, new AllResourcesPayload(Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), null));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceList = (ResourceList) context;
    Set<Resource> allResourceSet = resourceList.asResourceSet();
    assertThat(allResourceSet, hasSize(greaterThan(0)));
    assertThat(allResourceSet,
        hasItems(mapToResource("http://dbpedia.org/resource/Mexican_vihuela",
            "http://dbpedia.org/resource/Mbira",
            "http://dbpedia.org/resource/Dhol",
            "http://dbtune.org/musicbrainz/resource/instrument/134",
            "http://dbtune.org/musicbrainz/resource/instrument/357",
            "http://dbtune.org/musicbrainz/resource/performance/7141",
            "http://dbtune.org/musicbrainz/resource/performance/7373",
            "http://dbtune.org/musicbrainz/resource/performance/8311").toArray(new Resource[0])));
  }

  @Test
  public void getAllMusicInstruments_mustReturnContextWithAllMusicInstruments() {
    ExplorationContext context = allResources
        .apply(null, new AllResourcesPayload(
            Collections.singletonList(new Resource("http://purl.org/ontology/mo/Instrument")), null,
            null, null));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceList = (ResourceList) context;
    Set<Resource> allResourceSet = resourceList.asResourceSet();
    assertThat(allResourceSet, hasSize(greaterThan(0)));
    assertThat(allResourceSet,
        hasItems(mapToResource("http://dbpedia.org/resource/Lute",
            "http://dbpedia.org/resource/Harp",
            "http://dbtune.org/musicbrainz/resource/instrument/429",
            "http://dbtune.org/musicbrainz/resource/instrument/201",
            "http://dbpedia.org/resource/Guitarra_de_golpe").toArray(new Resource[0])));
    assertThat(allResourceSet,
        not(hasItem(new Resource("http://dbtune.org/musicbrainz/resource/performance/98375"))));
    assertThat(allResourceSet,
        not(hasItems(new Resource("http://dbtune.org/musicbrainz/resource/performance/6870"))));
  }

  @Test
  public void getAllWithoutMusicInstruments_mustReturnContextWithAllWithoutMusicInstruments() {
    ExplorationContext context = allResources
        .apply(null, new AllResourcesPayload(null,
            Collections.singletonList(new Resource("http://purl.org/ontology/mo/Instrument")),
            null, null));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceList = (ResourceList) context;
    Set<Resource> allResourceSet = resourceList.asResourceSet();
    assertThat(allResourceSet, hasSize(greaterThan(0)));
    assertThat(allResourceSet,
        hasItems(mapToResource("http://dbtune.org/musicbrainz/resource/performance/98375",
            "http://dbtune.org/musicbrainz/resource/performance/6870").toArray(new Resource[0])));
  }

  @Test
  public void getAllMusicInstrumentsWithoutClasses_mustReturnCorrespondingResult() {
    ExplorationContext context = allResources
        .apply(null, new AllResourcesPayload(
            Collections.singletonList(new Resource("http://purl.org/ontology/mo/Instrument")),
            Collections.singletonList(new Resource("http://www.w3.org/2000/01/rdf-schema#Class")),
            null, null));
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    ResourceList resourceList = (ResourceList) context;
    Set<Resource> allResourceSet = resourceList.asResourceSet();
    assertThat(allResourceSet, hasSize(greaterThan(0)));
    assertThat(allResourceSet,
        hasItems(mapToResource("http://dbtune.org/musicbrainz/resource/instrument/230",
            "http://dbpedia.org/resource/Uilleann_pipes", "http://dbpedia.org/resource/Haegeum",
            "http://dbpedia.org/resource/Jarana_huasteca",
            "http://dbpedia.org/resource/Dhadd").toArray(new Resource[0])));
    assertThat(allResourceSet, not(hasItem(new Resource("http://dbpedia.org/resource/Guitar"))));
    assertThat(allResourceSet, not(hasItem(new Resource("http://dbpedia.org/resource/Violin"))));
  }
}
