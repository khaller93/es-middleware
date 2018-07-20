package at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.conf.IndexedMemoryKnowledgeGraphConfig;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.CentralityMetricsService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class implements integration test cases for {@link at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.CentralityMetricsService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class,
    IndexedMemoryKnowledgeGraph.class, InMemoryGremlinDAO.class, CentralityCacheManagerStub.class,
    KGDAOConfig.class, IndexedMemoryKnowledgeGraphConfig.class})
@TestPropertySource(properties = {"esm.db.choice=IndexedMemoryDB"})
public class CentralityMetricsServiceTest {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  @Qualifier("getSparqlDAO")
  private KGSparqlDAO sparqlDAO;
  @Autowired
  @Qualifier("getGremlinDAO")
  private KGGremlinDAO gremlinDAO;
  @Autowired
  private GremlinService gremlinService;
  @Autowired
  private CacheManager cacheManager;
  @Autowired
  private ApplicationContext applicationContext;

  private CentralityMetricsService centralityMetricsService;

  @PostConstruct
  public void setUpInstance() throws Exception {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Before
  public void setUp() throws InterruptedException {
    musicPintaResource.waitForAllDAOsBeingReady();
    centralityMetricsService = new CentralityMetricsService(gremlinService, cacheManager,
        applicationContext);
  }

  @Test
  public void test_computePageRankForAllResources_mustReturnCorrespondingPageRank() {
    centralityMetricsService.computePageRank();
    List<Resource> resources = ((SelectQueryResult) sparqlDAO
        .query(
            "SELECT distinct ?resource WHERE { {?resource ?p1 ?o} UNION {?s ?p2 ?resource} . FILTER(isIRI(?resource))}",
            false)).value().stream().map(r -> new Resource((BlankNodeOrIRI) r.get("resource")))
        .collect(Collectors.toList());
    List<Pair<Resource, Double>> resourceList = resources.stream().map(
        resource -> new ImmutablePair<>(resource, centralityMetricsService.getPageRankOf(resource)))
        .sorted((e1, e2) -> -Double.compare(e1.getRight(), e2.getRight()))
        .collect(Collectors.toList());
    assertThat("Instrument class must be in the top 10.", resourceList.stream().limit(10)
            .map(r -> r.getKey().getId()).collect(Collectors.toList()),
        hasItem("http://purl.org/ontology/mo/Instrument"));
  }

  @Test
  public void test_computeDegreeMetrics_mustReturnAResult() {
    centralityMetricsService.computeDegree();
    List<Resource> resources = ((SelectQueryResult) sparqlDAO
        .query(
            "SELECT distinct ?resource WHERE { {?resource ?p1 ?o} UNION {?s ?p2 ?resource} . FILTER(isIRI(?resource))}",
            false)).value().stream().map(r -> new Resource((BlankNodeOrIRI) r.get("resource")))
        .collect(Collectors.toList());
    List<Pair<Resource, Long>> resourceList = resources.stream().map(
        resource -> new ImmutablePair<>(resource, centralityMetricsService.getDegreeOf(resource)))
        .sorted((e1, e2) -> -Double.compare(e1.getRight(), e2.getRight()))
        .collect(Collectors.toList());
    assertThat("'Percussion instruments' must be in the top 10.", resourceList.stream().limit(10)
            .map(r -> r.getKey().getId()).collect(Collectors.toList()),
        hasItem("http://dbtune.org/musicbrainz/resource/instrument/124"));
  }

  @Test
  @Ignore
  public void test_computeBetweeness_mustReturnAResult() {
    centralityMetricsService.computeDegree();
    List<Resource> resources = ((SelectQueryResult) sparqlDAO
        .query(
            "SELECT distinct ?resource WHERE { {?resource ?p1 ?o} UNION {?s ?p2 ?resource} . FILTER(isIRI(?resource))}",
            false)).value().stream().map(r -> new Resource((BlankNodeOrIRI) r.get("resource")))
        .collect(Collectors.toList());
    List<Pair<Resource, Long>> resourceList = resources.stream().map(
        resource -> new ImmutablePair<>(resource, centralityMetricsService.getDegreeOf(resource)))
        .sorted((e1, e2) -> -Double.compare(e1.getRight(), e2.getRight()))
        .collect(Collectors.toList());
    assertThat("'Percussion instruments' must be in the top 10.", resourceList.stream().limit(10)
            .map(r -> r.getKey().getId()).collect(Collectors.toList()),
        hasItem("http://dbtune.org/musicbrainz/resource/instrument/124"));
  }
}
