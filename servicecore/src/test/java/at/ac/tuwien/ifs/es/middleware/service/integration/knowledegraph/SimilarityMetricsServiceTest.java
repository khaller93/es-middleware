package at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimilarityMetricsService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class unit tests {@link SimilarityMetricsService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class, SimilarityMetricsService.class,
    IndexedMemoryKnowledgeGraph.class, InMemoryGremlinDAO.class, SimpleSPARQLService.class})
public class SimilarityMetricsServiceTest {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private KGSparqlDAO sparqlDAO;
  @Autowired
  private KGGremlinDAO gremlinDAO;
  @Autowired
  private SimilarityMetricsService similarityMetricsService;

  private static final Resource vielle = new Resource(
      BlankOrIRIJsonUtil.valueOf("http://dbtune.org/musicbrainz/resource/instrument/116"));
  private static final Resource violin = new Resource(
      BlankOrIRIJsonUtil.valueOf("http://dbtune.org/musicbrainz/resource/instrument/86"));
  private static List<ResourcePair> resourcePairList;

  @BeforeClass
  public static void setUpClass() throws Exception {
    resourcePairList = Arrays
        .asList(ResourcePair.of(vielle, vielle), ResourcePair.of(violin, vielle),
            ResourcePair.of(vielle, violin));
  }

  @PostConstruct
  public void setUp() throws Exception {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Test
  public void tests_getDistanceBetweenPairs() {
    Map<ResourcePair, Integer> distanceMap = similarityMetricsService.getDistance(resourcePairList);
    assertThat("The distance between same resource must be 0.",
        distanceMap.get(ResourcePair.of(vielle, vielle)), is(equalTo(0)));
    assertThat("The distance between vielle and violin must be 1, due to subclass relationship.",
        distanceMap.get(ResourcePair.of(vielle, violin)), is(equalTo(1)));
    assertThat("The distance between resources is symmetric.",
        distanceMap.get(ResourcePair.of(vielle, violin)),
        is(equalTo(distanceMap.get(ResourcePair.of(violin, vielle)))));
  }
}
