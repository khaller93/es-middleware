package at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.AbstractClonedGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.GremlinDAOUpdateEvent;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.InformationContentService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.SimpleGremlinService;
import at.ac.tuwien.ifs.es.middleware.testutil.MusicPintaInstrumentsResource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class implements integration test cases for {@link InformationContentServiceTest}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MusicPintaInstrumentsResource.class,
    KnowledgeGraphConfig.class, SimpleGremlinService.class, InformationContentService.class,
    IndexedMemoryKnowledgeGraph.class, InMemoryGremlinDAO.class})
@TestPropertySource(properties = {
    "esm.db.choice=IndexedMemoryDB",
    "esm.cache.enable=false"
})
public class InformationContentServiceTest {

  @Rule
  @Autowired
  public MusicPintaInstrumentsResource musicPintaResource;
  @Autowired
  private KnowledgeGraphDAO knowledgeGraphDAO;
  @Autowired
  private InformationContentService informationContentService;

  @Test
  public void test_getAllClasses_mustReturnInstrument() {
    List<Resource> allClasses = informationContentService.getAllClasses();
    assertThat(allClasses, not(hasSize(0)));
    assertThat(allClasses.stream().map(Resource::getId).collect(Collectors.toList()),
        hasItems("http://purl.org/ontology/mo/Instrument"));
  }

  @Test
  public void test_getInformationContent_mustReturnMap() {
    Map<Resource, Double> informationContentForClasses = informationContentService
        .getInformationContentForClasses();
    assertThat(informationContentForClasses.keySet().stream().map(Resource::getId)
        .collect(Collectors.toList()), hasItem("http://purl.org/ontology/mo/Instrument"));
    System.out.println(">>>" + informationContentForClasses.entrySet());
  }

}
