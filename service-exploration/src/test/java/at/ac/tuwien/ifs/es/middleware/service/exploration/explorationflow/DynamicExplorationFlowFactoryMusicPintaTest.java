package at.ac.tuwien.ifs.es.middleware.service.exploration.explorationflow;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStepPayload;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.PrimaryKGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.exploration.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.factory.DynamicExplorationFlowFactory;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition.FullTextSearch;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.exploitation.ResourceDescriber;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.FullTextSearchPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.exploitation.DescriberPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry;
import at.ac.tuwien.ifs.es.middleware.service.exploration.request.DynamicExplorationFlowRequest;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleFullTextSearchService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SimpleSPARQLService;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.IRI;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, RDF4JLuceneFullTextSearchDAO.class,
    DynamicExplorationFlowFactory.class, ExplorationFlowRegistry.class, FullTextSearch.class,
    ResourceDescriber.class, SimpleFullTextSearchService.class, ObjectMapper.class,
    PrimaryKGDAOConfig.class, RDF4JDAOConfig.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class DynamicExplorationFlowFactoryMusicPintaTest {

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private ExplorationFlowRegistry registry;
  @Autowired
  private DynamicExplorationFlowFactory factory;

  @Before
  public void setUp() {
    registry.register("esm.source.fts", FullTextSearch.class);
    registry.register("esm.exploit.describe", ResourceDescriber.class);
  }

  @Test
  public void test_validFlowSpecification_mustReturnCorrespondingFlow() throws IOException {
    DynamicExplorationFlowRequest request = objectMapper.readValue("{"
        + "\"steps\": [{"
        + "  \"name\": \"esm.source.fts\","
        + "  \"param\": {"
        + "    \"keyword\": \"guitar\","
        + "    \"limit\": 5"
        + "  }"
        + "},"
        + "{"
        + "  \"name\": \"esm.exploit.describe\""
        + "}]}", DynamicExplorationFlowRequest.class);
    ExplorationFlow explorationFlow = factory.constructFlow(request);
    assertNotNull(explorationFlow);
    List<Pair<ExplorationFlowStep, ExplorationFlowStepPayload>> flowStepList = explorationFlow.asList();
    assertThat("The returned flow must have two steps. A full-text-search and describe operation.",
        flowStepList, hasSize(2));
    // Check first fts step
    Pair<ExplorationFlowStep, ExplorationFlowStepPayload> ftsPair = explorationFlow.asList().get(0);
    assertThat(ftsPair.getValue0(), instanceOf(FullTextSearch.class));
    assertThat(ftsPair.getValue1(), instanceOf(FullTextSearchPayload.class));
    FullTextSearchPayload ftsPayload = (FullTextSearchPayload) ftsPair.getValue1();
    assertThat(ftsPayload.getKeyword(), is("guitar"));
    assertThat(ftsPayload.getLimit(), is(5));
    assertNull(ftsPayload.getOffset());
    // Check second describer step
    Pair<ExplorationFlowStep, ExplorationFlowStepPayload> describerPair = explorationFlow.asList().get(1);
    assertThat(describerPair.getValue0(), instanceOf(ResourceDescriber.class));
    assertThat(describerPair.getValue1(), instanceOf(DescriberPayload.class));
    DescriberPayload describerPayload = (DescriberPayload) describerPair.getValue1();
    assertThat(describerPayload.getContent().values().stream()
            .map(r -> ((IRI) r.getProperties().get(0).value()).getIRIString())
            .collect(Collectors.toList()),
        hasItems("http://www.w3.org/2000/01/rdf-schema#label",
            "http://www.w3.org/2000/01/rdf-schema#comment"));
  }

  @Test(expected = JsonMappingException.class)
  public void test_FlowSpecificationNoName_mustThrowException() throws IOException {
    objectMapper.readValue("{"
        + "\"steps\": [{"
        + "  \"param\": {"
        + "    \"keyword\": \"guitar\","
        + "    \"limit\": 2"
        + "  }"
        + "}]}", DynamicExplorationFlowRequest.class);
  }

  @Test(expected = ExplorationFlowSpecificationException.class)
  public void test_FlowSpecificationUnknownOperator_mustThrowException() throws IOException {
    DynamicExplorationFlowRequest request = objectMapper.readValue("{"
        + "\"steps\": [{"
        + "  \"name\": \"esm.source.fts\","
        + "  \"param\": {"
        + "    \"keyword\": \"guitar\","
        + "    \"limit\": 2"
        + "  }"
        + "},"
        + "{\"name\": \"esm.invalid.operator\"}"
        + "]}", DynamicExplorationFlowRequest.class);
    factory.constructFlow(request);
  }
}
