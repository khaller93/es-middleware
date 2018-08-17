package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation.normalization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.ThreadPoolConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JLuceneFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store.RDF4JMemoryStoreWithLuceneSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation.normalisation.MinMaxPayload;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.aggregation.normalisation.MinMaxPayload.MinMaxTarget;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aggregation.normalization.MinMaxNormalisation;
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
import org.junit.BeforeClass;
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
    ThreadPoolConfig.class, KGDAOConfig.class, RDF4JDAOConfig.class, MinMaxNormalisation.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class MinMaxNormalizationTests {

  private List<Resource> resourceList;
  private ResourceList resourceListContext;
  private Map<JsonPointer, List<Double>> resourceValueMap;

  @Before
  public void setUp() {
    resourceList = Lists
        .newArrayList(new Resource("http://dbpedia.org/resource/Violin"),
            new Resource("http://dbpedia.org/resource/Harp"),
            new Resource("http://dbpedia.org/resource/Ukulele"),
            new Resource("http://dbpedia.org/resource/Timpani"));
    resourceValueMap = new HashMap<>();
    resourceValueMap.put(JsonPointer.compile("/x/val"), Lists.newArrayList(-1.5, 0.5, 1.0, 2.0));
    resourceValueMap.put(JsonPointer.compile("/y/val"), Lists.newArrayList(1.0, 0.0, 1.0, 0.5));
    resourceValueMap.put(JsonPointer.compile("/z/val"), Lists.newArrayList(null, 2.0, null, 1.0));
    resourceValueMap.put(JsonPointer.compile("/o/val"), Lists.newArrayList(2.0, 2.0, 2.0, 2.0));
    resourceValueMap.put(JsonPointer.compile("/p/val"), Lists.newArrayList(null, null, null, null));
    resourceListContext = new ResourceList(resourceList);
    for (Map.Entry<JsonPointer, List<Double>> entry : resourceValueMap.entrySet()) {
      int n = 0;
      for (Resource resource : resourceList) {
        resourceListContext
            .putValuesData(resource.getId(), entry.getKey(),
                JsonNodeFactory.instance.numberNode(entry.getValue().get(n)));
        n++;
      }
    }
  }

  @Autowired
  private MinMaxNormalisation minMaxNormalisation;

  @Test
  public void computeMinMaxForContextWithoutMissingValues_mustReturnCorrectContext() {
    MinMaxTarget minMaxTargetX = new MinMaxTarget();
    minMaxTargetX.setMax(1.0);
    minMaxTargetX.setMin(0.0);
    minMaxTargetX.setPath(JsonPointer.compile("/x/val"));
    MinMaxTarget minMaxTargetY = new MinMaxTarget();
    minMaxTargetY.setMax(1.0);
    minMaxTargetY.setMin(0.0);
    minMaxTargetY.setPath(JsonPointer.compile("/y/val"));
    ExplorationContext context = minMaxNormalisation.apply(resourceListContext, new MinMaxPayload(
        Lists.newArrayList(minMaxTargetX, minMaxTargetY)));
    assertNotNull(context);
    List<Double> xPropValueList = resourceList.stream()
        .map(r -> ((DoubleNode) context.getValues(r.getId(), JsonPointer.compile("/x/val")).get())
            .asDouble()).collect(Collectors.toList());
    assertThat(xPropValueList, everyItem(allOf(greaterThanOrEqualTo(0.0), lessThanOrEqualTo(1.0))));
    assertThat(
        "The x-value of the first entry is the overall minimum and thus must be mapped to 0.0.",
        xPropValueList.get(0), equalTo(0.0));
    assertThat(
        "The x-value of the fourth entry is the overall maximum and thus must be mapped to 1.0.",
        xPropValueList.get(3), equalTo(1.0));
    List<Double> yPropValueList = resourceList.stream()
        .map(r -> ((DoubleNode) context.getValues(r.getId(), JsonPointer.compile("/y/val")).get())
            .asDouble()).collect(Collectors.toList());
    assertThat(yPropValueList, everyItem(allOf(greaterThanOrEqualTo(0.0), lessThanOrEqualTo(1.0))));
    assertThat(yPropValueList,
        contains(resourceValueMap.get(JsonPointer.compile("/y/val")).toArray()));
  }

  @Test
  public void computeMinMaxForPayloadWithoutTargets_mustKeepContextUnchanged() {
    ExplorationContext context = minMaxNormalisation
        .apply(resourceListContext, new MinMaxPayload(Lists.newArrayList()));
    assertNotNull(context);
    assertThat(resourceList.stream()
            .map(r -> resourceListContext.getValues(r.getId(), JsonPointer.compile("/x/val"))
                .orElse(null)).map(r -> r != null ? r.asDouble() : r).collect(Collectors.toList()),
        contains(resourceValueMap.get(JsonPointer.compile("/x/val")).toArray()));
  }

  @Test
  public void computeMinMaxForEmptyContext_mustReturnEmptyContext() {
    MinMaxTarget minMaxTargetX = new MinMaxTarget();
    minMaxTargetX.setMax(1.0);
    minMaxTargetX.setMin(0.0);
    minMaxTargetX.setPath(JsonPointer.compile("/x/val"));
    ResourceList context = (ResourceList) minMaxNormalisation
        .apply(new ResourceList(), new MinMaxPayload(Lists.newArrayList(minMaxTargetX)));
    assertNotNull(context);
    assertThat(context.asResourceSet(), hasSize(0));
  }

  @Test
  public void computeMinMaxForPropWithAllNullValues_mustReturnContextWithPropKeepingAllNullValues() {
    MinMaxTarget minMaxTargetX = new MinMaxTarget();
    minMaxTargetX.setMax(1.0);
    minMaxTargetX.setMin(0.0);
    minMaxTargetX.setPath(JsonPointer.compile("/p/val"));
    ResourceList context = (ResourceList) minMaxNormalisation
        .apply(resourceListContext, new MinMaxPayload(Lists.newArrayList(minMaxTargetX)));
    assertNotNull(context);
    List<Double> pPropValueList = resourceList.stream()
        .map(r -> (context.getValues(r.getId(), JsonPointer.compile("/p/val"))))
        .map(r -> r.get().isNumber() ? r.get().asDouble() : null)
        .collect(Collectors.toList());
    assertThat(pPropValueList, everyItem(equalTo(null)));
  }

  @Test
  public void computeMinMaxForValuePropWithMissingValues_mustIgnoreThemAndScaleExistingValues() {
    MinMaxTarget minMaxTargetZ = new MinMaxTarget();
    minMaxTargetZ.setMax(1.0);
    minMaxTargetZ.setMin(0.0);
    minMaxTargetZ.setPath(JsonPointer.compile("/z/val"));
    ResourceList context = (ResourceList) minMaxNormalisation
        .apply(resourceListContext, new MinMaxPayload(Lists.newArrayList(minMaxTargetZ)));
    assertNotNull(context);
    List<Double> zPropValueList = resourceList.stream()
        .map(r -> (context.getValues(r.getId(), JsonPointer.compile("/z/val"))))
        .map(r -> r.get().isNumber() ? r.get().asDouble() : null)
        .collect(Collectors.toList());
    assertNull(zPropValueList.get(0));
    assertNull(zPropValueList.get(2));
    assertThat(
        "The z-value of the second entry is the overall maximum and thus must be mapped to 1.0.",
        zPropValueList.get(1), equalTo(1.0));
    assertThat(
        "The z-value of the fourth entry is the overall minimum and thus must be mapped to 0.0.",
        zPropValueList.get(3), equalTo(0.0));
  }

  @Test
  public void computeMinMaxAllValuesSame_mustNotFailAndReturnCorrectResult() {
    MinMaxTarget minMaxTargetO = new MinMaxTarget();
    minMaxTargetO.setMax(1.0);
    minMaxTargetO.setMin(0.0);
    minMaxTargetO.setPath(JsonPointer.compile("/o/val"));
    ResourceList context = (ResourceList) minMaxNormalisation
        .apply(resourceListContext, new MinMaxPayload(Lists.newArrayList(minMaxTargetO)));
    assertNotNull(context);
    List<Double> oPropValueList = resourceList.stream()
        .map(r -> ((DoubleNode) context.getValues(r.getId(), JsonPointer.compile("/o/val")).get())
            .asDouble()).collect(Collectors.toList());
    assertThat(oPropValueList, contains(1.0, 1.0, 1.0, 1.0));
  }

}
