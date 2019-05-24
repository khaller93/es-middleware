package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.WeightedSumPayload;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.WeightedSum;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests the operator {@link WeightedSum}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, WeightedSum.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class WeightedSumTests {

  private List<Resource> resourceList;
  private ResourceList resourceListContext;
  private Map<JsonPointer, List<Double>> resourceValueMap;

  @Before
  public void setUpClass() {
    resourceList = Lists
        .newArrayList(new Resource("http://dbpedia.org/resource/Violin"),
            new Resource("http://dbpedia.org/resource/Harp"),
            new Resource("http://dbpedia.org/resource/Ukulele"),
            new Resource("http://dbpedia.org/resource/Timpani"),
            new Resource("http://dbpedia.org/resource/Banjo"));
    resourceValueMap = new HashMap<>();
    resourceValueMap
        .put(JsonPointer.compile("/x/val"), Lists.newArrayList(-1.5, 0.5, 0.8, 2.0, 1.0));
    resourceValueMap
        .put(JsonPointer.compile("/y/val"), Lists.newArrayList(1.0, -1.0, 2.0, null, 0.5));
    resourceValueMap
        .put(JsonPointer.compile("/z/val"), Lists.newArrayList(1.0, -1.0, 2.0, 1.5, 0.5));
    resourceListContext = new ResourceList(resourceList);
    for (Map.Entry<JsonPointer, List<Double>> entry : resourceValueMap.entrySet()) {
      int n = 0;
      for (Resource resource : resourceList) {
        resourceListContext
            .values().put(resource.getId(), entry.getKey(),
                JsonNodeFactory.instance.numberNode(entry.getValue().get(n)));
        n++;
      }
    }
  }

  @Autowired
  private WeightedSum weightedSum;

  public List<Resource> mapToResource(List<String> iriStrings) {
    return iriStrings.stream().map(Resource::new).collect(Collectors.toList());
  }

  public List<Resource> mapToResource(String... iriStrings) {
    return Stream.of(iriStrings).map(Resource::new).collect(Collectors.toList());
  }

  @Test
  public void weightThreeProps_mustReturnNewWeightedProp() {
    Map<JsonPointer, Double> weightMap = new HashMap<>();
    weightMap.put(JsonPointer.compile("/x/val"), 0.0);
    weightMap.put(JsonPointer.compile("/y/val"), 1.0);
    weightMap.put(JsonPointer.compile("/z/val"), -2.0);
    ExplorationContext context = weightedSum.apply(resourceListContext,
        new WeightedSumPayload(JsonPointer.compile("/weighted/val"), weightMap));
    assertNotNull(context);
    List<Double> weightedValues = resourceList.stream().map(
        res -> {
          Optional<JsonNode> valuesOpt = context
              .values().get(res.getId(), JsonPointer.compile("/weighted/val"));
          if (valuesOpt.isPresent() && valuesOpt.get().isNumber()) {
            return valuesOpt.get().asDouble();
          } else {
            return null;
          }
        }).collect(Collectors.toList());
    assertThat(weightedValues, contains(-1.0, 1.0, -2.0, null, -0.5));
  }

  @Test(expected = ExplorationFlowSpecificationException.class)
  public void weightUnknownProp_throwsException() {
    Map<JsonPointer, Double> weightMap = new HashMap<>();
    weightMap.put(JsonPointer.compile("/x/val"), 0.0);
    weightMap.put(JsonPointer.compile("/unknown/val"), 1.0);
    weightMap.put(JsonPointer.compile("/z/val"), -2.0);
    weightedSum.apply(resourceListContext,
        new WeightedSumPayload(JsonPointer.compile("/weighted/val"), weightMap));
  }

  @Test
  public void weightsEmptyPropMap_mustReturnContextWithNewPropAndAll0() {
    ExplorationContext context = weightedSum.apply(resourceListContext,
        new WeightedSumPayload(JsonPointer.compile("/weighted/val"), Collections.emptyMap()));
    List<Double> weightedValues = resourceList.stream().map(
        res -> {
          Optional<JsonNode> valuesOpt = context
              .values().get(res.getId(), JsonPointer.compile("/weighted/val"));
          if (valuesOpt.isPresent() && valuesOpt.get().isNumber()) {
            return valuesOpt.get().asDouble();
          } else {
            return null;
          }
        }).collect(Collectors.toList());
    assertThat(weightedValues, contains(0.0, 0.0, 0.0, 0.0, 0.0));
  }

  @Test(expected = ExplorationFlowSpecificationException.class)
  public void weightWithEmptyResultPropPath_throwsException() {
    Map<JsonPointer, Double> weightMap = new HashMap<>();
    weightMap.put(JsonPointer.compile("/x/val"), 0.0);
    weightMap.put(JsonPointer.compile("/unknown/val"), 1.0);
    weightMap.put(JsonPointer.compile("/z/val"), -2.0);
    weightedSum.apply(resourceListContext,
        new WeightedSumPayload(JsonPointer.compile("/"), weightMap));
  }

}
