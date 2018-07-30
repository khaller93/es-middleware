package at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.centrality.CentralityMetricsService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class implements integration test cases for {@link CentralityMetricsService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    CentralityCacheManagerStub.class, ThreadPoolConfig.class, KGDAOConfig.class,
    RDF4JDAOConfig.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
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
  private TaskExecutor taskExecutor;
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
        applicationContext, taskExecutor);
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
    List<Map<String, RDFTerm>> result = ((SelectQueryResult) sparqlDAO
        .query("select ?o (count(*) as ?c) where { \n"
            + "    ?s ?p ?o .\n"
            + "    FILTER (isIRI(?o)) .\n"
            + "}\n"
            + "GROUP BY ?o\n"
            + "ORDER BY DESC(?c)\n"
            + "LIMIT 10", true)).value();
    List<Long> sparqlDegreeCount = new LinkedList<>();
    List<Long> gremlinDegreeCount = new LinkedList<>();
    for (Map<String, RDFTerm> row : result) {
      sparqlDegreeCount.add(Long.parseLong(((Literal) row.get("c")).getLexicalForm()));
      gremlinDegreeCount
          .add(centralityMetricsService.getDegreeOf(new Resource((IRI) row.get("o"))));
    }
    assertThat(gremlinDegreeCount, hasItems(sparqlDegreeCount.toArray(new Long[0])));
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
