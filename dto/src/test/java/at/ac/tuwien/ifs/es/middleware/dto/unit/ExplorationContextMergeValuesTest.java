package at.ac.tuwien.ifs.es.middleware.dto.unit;


import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class ExplorationContextMergeValuesTest<T extends IdentifiableResult> {

  private ExplorationContext<T> explorationContext;
  private Map<String, ObjectNode> valuesMapSource;
  private Map<String, ObjectNode> valuesMapToMerge;


  protected abstract ExplorationContext<T> getContext();

  @Before
  public void setUp() throws Exception {
    explorationContext = getContext();
    explorationContext.clearValues();
    valuesMapSource = new HashMap<>();
    valuesMapToMerge = new HashMap<>();
    /* 'a' resource */
    ObjectNode aNode = JsonNodeFactory.instance.objectNode();
    ObjectNode aCentralityNode = JsonNodeFactory.instance.objectNode();
    aCentralityNode.set("pageRank", JsonNodeFactory.instance.numberNode(2.0));
    aCentralityNode.set("degree", JsonNodeFactory.instance.numberNode(4));
    aNode.set("centrality", aCentralityNode);
    ObjectNode aSimilarityNode = JsonNodeFactory.instance.objectNode();
    aSimilarityNode.set("ldsd", JsonNodeFactory.instance.numberNode(0.25));
    aSimilarityNode.set("distance", JsonNodeFactory.instance.numberNode(24));
    ArrayNode sameAsNode = JsonNodeFactory.instance.arrayNode();
    sameAsNode.add("test://e");
    sameAsNode.add("test://f");
    sameAsNode.add("test://g");
    aNode.set("similarity", aSimilarityNode);
    valuesMapSource.put("test://a", aNode);
    /* 'b' resource */
    ObjectNode bNode = JsonNodeFactory.instance.objectNode();
    ObjectNode bCentralityNode = JsonNodeFactory.instance.objectNode();
    bCentralityNode.set("pageRank", JsonNodeFactory.instance.numberNode(2.0));
    bCentralityNode.set("degree", JsonNodeFactory.instance.numberNode(4));
    bNode.set("centrality", bCentralityNode);
    ObjectNode bSimilarityNode = JsonNodeFactory.instance.objectNode();
    bSimilarityNode.set("ldsd", JsonNodeFactory.instance.numberNode(0.25));
    bSimilarityNode.set("distance", JsonNodeFactory.instance.numberNode(24));
    bNode.set("similarity", bSimilarityNode);
    valuesMapSource.put("test://b", bNode);
    /* 'c' resource */
    valuesMapSource.put("test://c", JsonNodeFactory.instance.objectNode());
    for (String key : valuesMapSource.keySet()) {
      explorationContext.putValuesData(key, ExplorationContext.ROOT_PTR, valuesMapSource.get(key));
    }
    /* 'a' resource in map to merge */
    ObjectNode aNodeV2 = JsonNodeFactory.instance.objectNode();
    ObjectNode aCentralityNodeV2 = JsonNodeFactory.instance.objectNode();
    aCentralityNodeV2.set("ic", JsonNodeFactory.instance.numberNode(1.0));
    aCentralityNodeV2.set("pageRank", JsonNodeFactory.instance.numberNode(4.0));
    aNodeV2.set("centrality", aCentralityNodeV2);
    ObjectNode aSimilarityNodeV2 = JsonNodeFactory.instance.objectNode();
    aSimilarityNodeV2.set("resnik", JsonNodeFactory.instance.numberNode(2.25));
    aNodeV2.set("similarity", aSimilarityNodeV2);
    valuesMapToMerge.put("test://a", aNodeV2);
    /* 'e' resource in map to merge */
    valuesMapToMerge.put("test://e", JsonNodeFactory.instance.objectNode());
  }

  @Test
  public void test_mergeWithEmptyMap_mustKeepContextValuesUnchanged() {
    explorationContext.mergeValues(new HashMap<>());
    Map<String, ObjectNode> allValuesMap = explorationContext.getAllValues();
    assertThat(allValuesMap.keySet(),
        containsInAnyOrder(valuesMapSource.keySet().toArray(new String[0])));
    Optional<JsonNode> pageRankNumberNode = explorationContext
        .getValues("test://a", JsonPointer.compile("/centrality/pageRank"));
    assertTrue(pageRankNumberNode.isPresent());
    assertThat(pageRankNumberNode.get().asDouble(), is(2.0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_mergeWithNull_mustThrowIllegalArgumentException() {
    explorationContext.mergeValues(null);
  }

  @Test
  public void test_mergeWithValuesMap_mustResultInContextWithMergedValues() {
    explorationContext.mergeValues(valuesMapToMerge);
    Map<String, ObjectNode> allValuesMap = explorationContext.getAllValues();
    assertThat(allValuesMap.keySet(),
        hasItems(valuesMapSource.keySet().toArray(new String[0])));
    assertThat(allValuesMap.keySet(),
        hasItems(valuesMapToMerge.keySet().toArray(new String[0])));
    /* value map to merge must take precedence */
    Optional<JsonNode> pageRankNumberNode = explorationContext
        .getValues("test://a", JsonPointer.compile("/centrality/pageRank"));
    assertTrue(pageRankNumberNode.isPresent());
    assertThat(pageRankNumberNode.get().asDouble(), is(4.0));
    /* centrality object node must be merged */
    Optional<JsonNode> centralityNode = explorationContext
        .getValues("test://a", JsonPointer.compile("/centrality"));
    assertTrue(centralityNode.isPresent());
    assertThat(Lists.newArrayList(centralityNode.get().fieldNames()),
        containsInAnyOrder("pageRank", "degree", "ic"));
    /* similarity object node must be merged */
    Optional<JsonNode> similarityNode = explorationContext
        .getValues("test://a", JsonPointer.compile("/similarity"));
    assertTrue(similarityNode.isPresent());
    assertThat(Lists.newArrayList(similarityNode.get().fieldNames()),
        containsInAnyOrder("ldsd", "distance", "resnik"));
  }

  @Test
  public void test_mergeEmptyContextWithValuesMap_mustReturnContextWithValuesMapEntries() {
    explorationContext.clearValues();
    assertThat(explorationContext.getAllValues().keySet(), empty());
    explorationContext.mergeValues(valuesMapToMerge);
    assertThat(explorationContext.getAllValues().keySet(),
        containsInAnyOrder(valuesMapToMerge.keySet().toArray(new String[0])));
  }
}
