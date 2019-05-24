package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.context;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box.ValueBox;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * This class should test functionality of {@link ValueBoxTest}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class ValueBoxTest {

  private ObjectMapper objectMapper = new ObjectMapper();

  private ValueBox valueBox;
  private ValueBox otherValueBox;

  private ObjectNode centralityObj;
  private ObjectNode similarityObj;

  private JsonNode aNode;
  private JsonNode aNode1;
  private JsonNode aNode2;
  private JsonNode bNode;
  private JsonNode cNode;
  private JsonNode eNode;

  /**
   * Should return a new instance of the {@link ValueBox} that shall be tested.
   *
   * @return a new instance of {@link ValueBox} that shall be tested.
   */
  protected abstract ValueBox getValueBox();

  @Before
  public void setUp() throws Exception {
    this.valueBox = getValueBox();
    this.otherValueBox = getValueBox();
    /* centrality dummy values */
    centralityObj = (ObjectNode) objectMapper.readTree("{\n"
        + "    \"acs\": 1.4,\n"
        + "    \"load\": 2.1,\n"
        + "    \"pageRank\": 4.5,\n"
        + "    \"singleNodeLoad\": 2.4\n"
        + "}");
    /* similarity dummy values */
    similarityObj = (ObjectNode) objectMapper.readTree("{\n"
        + "    \"icpr\": 0.4,\n"
        + "    \"ldsd\": 0.9\n"
        + "}");
    /* resource a dummy values */
    aNode = objectMapper.readTree("{\n"
        + "    \"centrality\": {\n"
        + "        \"pageRank\": 2.0,\n"
        + "        \"degree\": 4\n"
        + "    },\n"
        + "    \"similarity\": {\n"
        + "        \"ldsd\": 0.25,\n"
        + "        \"distance\": 24\n"
        + "    },\n"
        + "    \"sameAs\": [\"test://e\",\"test://f\",\"test://g\"]\n"
        + "}");
    aNode1 = objectMapper.readTree("{\n"
        + "    \"centrality\": {\n"
        + "        \"pageRank\": 4.0,\n"
        + "        \"ic\": 1.0\n"
        + "    },\n"
        + "    \"similarity\": {\n"
        + "        \"resnik\": 2.25\n"
        + "    }\n"
        + "}");
    aNode2 = objectMapper.readTree("{\n"
        + "    \"sameAs\": \"overwritten\""
        + "}");
    /* resource b dummy values */
    bNode = objectMapper.readTree("{\n"
        + "    \"centrality\": {\n"
        + "        \"pageRank\": 2.0,\n"
        + "        \"degree\": 4\n"
        + "    },\n"
        + "    \"similarity\": {\n"
        + "        \"ldsd\": 0.25,\n"
        + "        \"distance\": 24\n"
        + "    },\n"
        + "    \"sameAs\": [\"test://e\",\"test://f\",\"test://g\"]\n"
        + "}");
    /* resource c dummy values */
    cNode = JsonNodeFactory.instance.nullNode();
    /* resource e dummy values */
    eNode = JsonNodeFactory.instance.nullNode();
  }

  @Test
  public void test_getNames_mustReturnNameList() {
    valueBox.put("a", centralityObj);
    valueBox.put("b", centralityObj);
    valueBox.put("z", similarityObj);

    Collection<String> names = valueBox.names();
    assertNotNull(names);
    assertThat(names, containsInAnyOrder("a", "b", "z"));
  }

  @Test
  public void test_asMap_mustReturnMap() {
    valueBox.put("a", centralityObj);
    valueBox.put("b", centralityObj);
    valueBox.put("z", similarityObj);

    Map<String, JsonNode> valuesMap = valueBox.asMap();
    assertNotNull(valuesMap);
    assertThat(valuesMap.keySet(), containsInAnyOrder("a", "b", "z"));

    JsonNode jsonNode = valuesMap.get("b");
    assertNotNull(jsonNode);
    assertThat(jsonNode.at("/pageRank").asDouble(), is(4.5));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_putNullToRoot_mustThrowIllegalArgumentException() {
    valueBox.put("a", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_putDataToNullName_mustThrowIllegalArgumentException() {
    valueBox.put(null, centralityObj);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_putDataToEmptyName_mustThrowIllegalArgumentException() {
    valueBox.put("", centralityObj);
  }

  @Test
  public void test_putDataWithName_mustPersist() {
    valueBox.put("abc", centralityObj);

    assertThat(valueBox.names(), hasItem("abc"));

    Optional<JsonNode> optionalJsonNode = valueBox.get("abc");
    assertTrue(optionalJsonNode.isPresent());
    assertThat(optionalJsonNode.get().at("/singleNodeLoad").asDouble(), is(2.4));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_putNullToRootWithPointer_mustThrowIllegalArgumentException() {
    valueBox.put("a", ValueBox.ROOT_PTR, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_putDataToNullNameWithPointer_mustThrowIllegalArgumentException() {
    valueBox.put(null, ValueBox.ROOT_PTR, centralityObj);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_putDataToEmptyNameWithPointer_mustThrowIllegalArgumentException() {
    valueBox.put("", ValueBox.ROOT_PTR, centralityObj);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_putDataToNameWithNullPointer_mustThrowIllegalArgumentException() {
    valueBox.put("a", null, centralityObj);
  }

  @Test
  public void test_putNewDataWithPointer_mustPersist() {
    valueBox.put("z", JsonPointer.compile("/info/sim"), centralityObj);

    assertThat(valueBox.names(), hasItem("z"));
    Optional<JsonNode> jsonNodeOptional = valueBox.get("z");
    assertTrue(jsonNodeOptional.isPresent());
    assertThat(jsonNodeOptional.get().at("/info/sim/load").asDouble(), is(2.1));
  }

  @Test
  public void test_putNewDataWithPointer2_mustPersist() {
    valueBox.put("z", centralityObj);
    valueBox.put("z", JsonPointer.compile("/sum"), JsonNodeFactory.instance.numberNode(10.4));

    assertThat(valueBox.names(), hasItem("z"));
    Optional<JsonNode> jsonNodeOptional = valueBox.get("z");
    assertTrue(jsonNodeOptional.isPresent());
    assertThat(jsonNodeOptional.get().at("/sum").asDouble(), is(10.4));
    assertThat(jsonNodeOptional.get().at("/load").asDouble(), is(2.1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getDataOfNullName_mustThrowIllegalArgumentException() {
    valueBox.get(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getDataOfEmptyName_mustThrowIllegalArgumentException() {
    valueBox.get("");
  }

  @Test
  public void test_getDataOfName_mustReturnCorrespondingDataNode() {
    valueBox.put("a", similarityObj);

    Optional<JsonNode> jsonNodeOptional = valueBox.get("a");
    assertTrue(jsonNodeOptional.isPresent());
    assertThat(jsonNodeOptional.get().at("/ldsd").asDouble(), is(0.9));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getDataOfNullNameWithPointer_mustThrowIllegalArgumentException() {
    valueBox.get(null, ValueBox.ROOT_PTR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getDataOfEmptyNameWithPointer_mustThrowIllegalArgumentException() {
    valueBox.get("", ValueBox.ROOT_PTR);
  }

  @Test
  public void test_getDataOfNameWithPointer_mustReturnCorrespondingDataNode() {
    valueBox.put("a", similarityObj);

    Optional<JsonNode> jsonNodeOptional = valueBox.get("a", JsonPointer.compile("/ldsd"));
    assertTrue(jsonNodeOptional.isPresent());
    assertTrue(jsonNodeOptional.get().isNumber());
    assertThat(jsonNodeOptional.get().asDouble(), is(0.9));
  }

  @Test
  public void test_mergeWithValuesBox_mustResultInContextWithMergedValues() {
    valueBox.put("test://a", aNode);
    valueBox.put("test://b", bNode);
    valueBox.put("test://c", cNode);
    otherValueBox.put("test://a", aNode1);
    otherValueBox.put("test://e", eNode);

    ValueBox mergedBox = valueBox.merge(otherValueBox);

    assertThat(valueBox.names(), hasItems("test://a", "test://b", "test://c"));
    assertThat(otherValueBox.names(), hasItems("test://a", "test://e"));
    assertThat(mergedBox.names(), hasItems("test://a", "test://b", "test://c", "test://e"));

    /* value box to merge must take precedence */
    Optional<JsonNode> pageRankNumberNode = mergedBox
        .get("test://a", JsonPointer.compile("/centrality/pageRank"));
    assertTrue(pageRankNumberNode.isPresent());
    assertThat(pageRankNumberNode.get().asDouble(), Matchers.is(4.0));
    /* centrality object node must be merged */
    Optional<JsonNode> centralityNode = mergedBox
        .get("test://a", JsonPointer.compile("/centrality"));
    assertTrue(centralityNode.isPresent());
    assertThat(Lists.newArrayList(centralityNode.get().fieldNames()),
        containsInAnyOrder("pageRank", "degree", "ic"));
    /* similarity object node must be merged */
    Optional<JsonNode> similarityNode = mergedBox
        .get("test://a", JsonPointer.compile("/similarity"));
    assertTrue(similarityNode.isPresent());
    assertThat(Lists.newArrayList(similarityNode.get().fieldNames()),
        containsInAnyOrder("ldsd", "distance", "resnik"));
  }

  @Test
  public void test_mergeWithValuesBoxOverwriteArray_mustResultInContextWithMergedValues() {
    valueBox.put("test://a", aNode);
    otherValueBox.put("test://a", aNode2);

    ValueBox mergedBox = valueBox.merge(otherValueBox);
    Optional<JsonNode> jsonNodeOptional = mergedBox.get("test://a");

    assertTrue(jsonNodeOptional.isPresent());
    assertThat(jsonNodeOptional.get().at("/sameAs").asText(), is("overwritten"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_removeDateOfNullName_mustThrowIllegalArgumentException() {
    valueBox.remove(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_removeDateOfEmptyName_mustThrowIllegalArgumentException() {
    valueBox.remove("");
  }

  @Test
  public void test_removeDateOfNameA_mustRemoveData() {
    valueBox.put("a", centralityObj);

    assertThat(valueBox.names(), hasItem("a"));
    valueBox.remove("a");
    assertThat(valueBox.names(), not(hasItem("a")));
  }

  @Test
  public void test_clearValueBox_mustBeEmptyAfterwards() {
    valueBox.put("a", centralityObj);
    valueBox.put("d", similarityObj);

    assertThat(valueBox.asMap().entrySet(), hasSize(greaterThanOrEqualTo(2)));
    valueBox.clear();
    assertThat(valueBox.names(), hasSize(0));
    assertThat(valueBox.asMap().entrySet(), hasSize(0));
  }
}
