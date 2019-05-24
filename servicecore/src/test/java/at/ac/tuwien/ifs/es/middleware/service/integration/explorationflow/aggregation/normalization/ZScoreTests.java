package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation.normalization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNotNull;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.aggregation.normalisation.ZScorePayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.normalization.MinMaxNormalisation;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aggregation.normalization.ZScore;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SimpleSPARQLService;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests the operator {@link MinMaxNormalisation}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleSPARQLService.class, RDF4JLuceneFullTextSearchDAO.class,
    RDF4JMemoryStoreWithLuceneSparqlDAO.class, ClonedInMemoryGremlinDAO.class,
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, ZScore.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class ZScoreTests {

  private List<Resource> resourceList;
  private ResourceList resourceListContext;
  private Map<JsonPointer, List<Double>> resourceValueMap;

  @Before
  public void setUp() {
    resourceList = Lists
        .newArrayList(new Resource("http://dbpedia.org/resource/Violin"),
            new Resource("http://dbpedia.org/resource/Harp"),
            new Resource("http://dbpedia.org/resource/Ukulele"),
            new Resource("http://dbpedia.org/resource/Timpani"),
            new Resource("http://dbpedia.org/resource/Banjo"),
            new Resource("http://dbpedia.org/resource/Guitar"),
            new Resource("http://dbpedia.org/resource/Mbira"),
            new Resource("http://dbpedia.org/resource/Fiddle"),
            new Resource("http://dbpedia.org/resource/Pipa"),
            new Resource("http://dbpedia.org/resource/Taiko"));
    resourceValueMap = new HashMap<>();
    resourceValueMap.put(JsonPointer.compile("/x/val"),
        Lists.newArrayList(0.0, 0.5, 1.0, 2.0, 1.2, 1.6, 20.0, 2.1, 0.4,
            1.0)); //average: 2,98 standard deviation: 5,709781082
    resourceValueMap.put(JsonPointer.compile("/y/val"),
        Lists.newArrayList(1.0, 0.0, 1.0, 0.5, 0.75, 0.33, 0.2, 0.05, 0.99, 0.25));
    resourceValueMap.put(JsonPointer.compile("/z/val"),
        Lists.newArrayList(null, 2.0, null, 2.0, null, -2.0, null, -2.0, null, null));
    resourceValueMap.put(JsonPointer.compile("/o/val"),
        Lists.newArrayList(2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0));
    resourceValueMap.put(JsonPointer.compile("/p/val"),
        Lists.newArrayList(null, null, null, null, null, null, null, null, null, null));
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
  private ZScore zScore;

  @Test
  public void computeZScoreForContextWithoutMissingValues_mustReturnCorrectContext() {
    ExplorationContext context = zScore
        .apply(resourceListContext, new ZScorePayload(
            Lists.newArrayList(JsonPointer.compile("/x/val"), JsonPointer.compile("/y/val"))));
    assertNotNull(context);
    List<Double> xPropValueList = resourceList.stream()
        .map(r -> ((DoubleNode) context.values().get(r.getId(), JsonPointer.compile("/x/val")).get())
            .asDouble())
        .collect(Collectors.toList());
    assertThat(xPropValueList, everyItem(lessThan(3.0)));
    assertThat(xPropValueList, everyItem(greaterThan(-0.53)));

  }

  @Test
  public void computeZScoreForPayloadWithoutTargets_mustKeepContextUnchanged() {
    ExplorationContext context = zScore
        .apply(resourceListContext, new ZScorePayload(Lists.emptyList()));
    assertNotNull(context);
    assertThat(resourceList.stream()
            .map(r -> resourceListContext.values().get(r.getId(), JsonPointer.compile("/x/val"))
                .orElse(null)).map(r -> r != null ? r.asDouble() : r).collect(Collectors.toList()),
        contains(resourceValueMap.get(JsonPointer.compile("/x/val")).toArray()));
    assertThat(resourceList.stream()
            .map(r -> resourceListContext.values().get(r.getId(), JsonPointer.compile("/y/val"))
                .orElse(null)).map(r -> r != null ? r.asDouble() : r).collect(Collectors.toList()),
        contains(resourceValueMap.get(JsonPointer.compile("/y/val")).toArray()));
  }

  @Test
  public void computeZScoreForEmptyContext_mustReturnEmptyContext() {
    ResourceList context = (ResourceList) zScore
        .apply(new ResourceList(),
            new ZScorePayload(Lists.newArrayList(JsonPointer.compile("/x/val"))));
    assertNotNull(context);
    assertThat(context.asResourceSet(), hasSize(0));
  }

  @Test
  public void computeMinMaxForPropWithAllNullValues_mustReturnContextWithPropKeepingAllNullValues() {
    ResourceList context = (ResourceList) zScore
        .apply(resourceListContext,
            new ZScorePayload(Lists.newArrayList(JsonPointer.compile("/p/val"))));
    assertNotNull(context);
    List<Double> pPropValueList = resourceList.stream()
        .map(r -> (context.values().get(r.getId(), JsonPointer.compile("/p/val"))))
        .map(r -> r.get().isNumber() ? r.get().asDouble() : null)
        .collect(Collectors.toList());
    assertThat(pPropValueList, everyItem(equalTo(null)));
  }

  @Test
  public void computeMinMaxForValuePropWithMissingValues_mustIgnoreThemAndScaleExistingValues() {
    ResourceList context = (ResourceList) zScore
        .apply(resourceListContext,
            new ZScorePayload(Lists.newArrayList(JsonPointer.compile("/z/val"))));
    assertNotNull(context);
    List<Double> zPropValueList = resourceList.stream()
        .map(r -> (context.values().get(r.getId(), JsonPointer.compile("/z/val"))))
        .map(r -> r.get().isNumber() ? r.get().asDouble() : null)
        .collect(Collectors.toList());
    assertThat(zPropValueList, contains(null, 1.0, null, 1.0, null, -1.0, null, -1.0, null, null));
  }

  @Test
  public void computeZScoreAllValuesSame_mustNotFailAndReturnCorrectResult() {
    ResourceList context = (ResourceList) zScore
        .apply(resourceListContext,
            new ZScorePayload(Lists.newArrayList(JsonPointer.compile("/o/val"))));
    assertNotNull(context);
    List<Double> oPropValueList = resourceList.stream()
        .map(r -> ((DoubleNode) context.values().get(r.getId(), JsonPointer.compile("/o/val")).get())
            .asDouble()).collect(Collectors.toList());
    assertThat(oPropValueList, contains(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
  }
}
