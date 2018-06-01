package at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.CentralityMetricsService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
 * This class implements integration test cases for {@link at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.CentralityMetricsService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleGremlinService.class, CentralityMetricsService.class,
    IndexedMemoryKnowledgeGraph.class, InMemoryGremlinDAO.class})
public class CentralityMetricsServiceTest {

  @Rule
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  public KGSparqlDAO sparqlDAO;
  @Autowired
  public KGGremlinDAO gremlinDAO;
  @Autowired
  private CentralityMetricsService centralityMetricsService;

  @PostConstruct
  public void setUp() throws Exception {
    musicPintaResource = new MusicPintaInstrumentsResource(sparqlDAO, gremlinDAO);
  }

  @Test
  public void test_computePageRankForAllResources_mustReturnCorrespondingPageRank() {
    Map<Resource, Double> pageRankMap = centralityMetricsService
        .getPageRank(Collections.emptyList());
    List<Entry<Resource, Double>> resourceList = pageRankMap.entrySet().stream()
        .sorted((e1, e2) -> -Double.compare(e1.getValue(), e2.getValue()))
        .collect(Collectors.toList());
    assertThat("Instrument class must be in the top 10.", resourceList.stream().limit(10)
            .map(r -> r.getKey().getId()).collect(Collectors.toList()),
        hasItem("http://purl.org/ontology/mo/Instrument"));
  }

  @Test
  public void test_computePageRankForInstruments_mustReturnCorrespondingPageRank() {
    List<Resource> classes = Stream
        .of("http://purl.org/ontology/mo/Instrument", "http://purl.org/ontology/mo/Performance")
        .map(r -> new Resource(BlankOrIRIJsonUtil.valueOf(r))).collect(Collectors.toList());
    Map<Resource, Double> pageRankMap = centralityMetricsService.getPageRank(classes);
    List<Entry<Resource, Double>> resourceList = pageRankMap.entrySet().stream()
        .sorted((e1, e2) -> -Double.compare(e1.getValue(), e2.getValue()))
        .collect(Collectors.toList());
    assertThat(resourceList.stream().limit(25)
            .map(r -> r.getKey().getId()).collect(Collectors.toList()),
        hasItems("http://dbtune.org/musicbrainz/resource/instrument/14"));
  }

  @Test
  public void test_computePageRankForUnknownClass_mustReturnEmptyMap() {
    Map<Resource, Double> pageRankMap = centralityMetricsService
        .getPageRank(
            Collections.singletonList(new Resource(BlankOrIRIJsonUtil.valueOf("test://unknown"))));
    assertThat(pageRankMap.entrySet(), hasSize(0));
  }

}
