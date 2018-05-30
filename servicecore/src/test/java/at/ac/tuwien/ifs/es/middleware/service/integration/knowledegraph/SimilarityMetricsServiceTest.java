package at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
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
@ContextConfiguration(classes = {MusicPintaInstrumentsResource.class,
    KnowledgeGraphConfig.class, SimpleGremlinService.class, SimilarityMetricsService.class,
    IndexedMemoryKnowledgeGraph.class, InMemoryGremlinDAO.class, SimpleSPARQLService.class})
@TestPropertySource(properties = {
    "esm.db.choice=IndexedMemoryDB",
    "esm.cache.enable=false"
})
public class SimilarityMetricsServiceTest {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private KnowledgeGraphDAO knowledgeGraphDAO;
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
