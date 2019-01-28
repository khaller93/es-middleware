package at.ac.tuwien.ifs.es.middleware.dto.unit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

/**
 * This class should test functionality of {@link at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractExplorationContextTest<T extends IdentifiableResult> {

  protected ExplorationContext<T> explorationContext;

  private ObjectNode centralityObj;
  private ObjectNode similarityObj;

  /**
   * Should return a new instance of the context that shall be tested.
   *
   * @return a new instance of {@link ExplorationContext} that shall be tested.
   */
  protected abstract ExplorationContext<T> getContext();

  @Before
  public void setUp() throws Exception {
    this.explorationContext = getContext();
    /* centrality dummy values */
    centralityObj = JsonNodeFactory.instance.objectNode();
    centralityObj.set("acs", JsonNodeFactory.instance.numberNode(1.4));
    centralityObj.set("load", JsonNodeFactory.instance.numberNode(2.1));
    centralityObj.set("pageRank", JsonNodeFactory.instance.numberNode(4.5));
    centralityObj.set("singleNodeLoad", JsonNodeFactory.instance.numberNode(2.4));
    /*similarity dummy values */
    similarityObj = JsonNodeFactory.instance.objectNode();
    similarityObj.set("icpr", JsonNodeFactory.instance.numberNode(0.4));
    similarityObj.set("ldsd", JsonNodeFactory.instance.numberNode(0.9));
  }

  @Test
  public void test_addMetadata_mustBePersistent() throws Exception {
    explorationContext.setMetadataFor("number", JsonNodeFactory.instance.numberNode(1000));
    Optional<JsonNode> optionalNumber = explorationContext.getMetadataFor("number");
    assertTrue(optionalNumber.isPresent());
    assertThat("The number metadata must be persisted.", optionalNumber.get().asInt(),
        is(equalTo(1000)));
    assertThat(explorationContext.getMetadataEntryNames(), contains("number"));
  }

  @Test
  public void test_removeMetadata_mustBeRemoved() throws Exception {
    explorationContext.setMetadataFor("number", JsonNodeFactory.instance.numberNode(1000));
    explorationContext.removeMetadataFor("number");
    assertFalse(explorationContext.getMetadataFor("number").isPresent());
    assertThat(explorationContext.getMetadataEntryNames(), hasSize(0));
  }

  @Test
  public void test_getMetadata_mustReturnDeepCopy() throws Exception {
    explorationContext.setMetadataFor("centrality", centralityObj);
    assertThat(explorationContext.getMetadataEntryNames(), containsInAnyOrder("centrality"));
    Map<String, JsonNode> copiedMetadata = explorationContext.getMetadata();
    centralityObj.set("acs", JsonNodeFactory.instance.numberNode(42.0));
    assertThat(copiedMetadata.get("centrality").get("acs").asDouble(), is(equalTo(1.4)));
  }

  @Test
  public void test_addMultipleEntries_mustBePersistent() throws Exception {
    explorationContext.setMetadataFor("number", JsonNodeFactory.instance.numberNode(1000));
    explorationContext.setMetadataFor("timestamp", JsonNodeFactory.instance.numberNode(100000L));
    assertThat(explorationContext.getMetadataEntryNames(),
        containsInAnyOrder("number", "timestamp"));
  }

  @Test
  public void test_replaceOldMetadata_mustBeNewValueAfterwards() throws Exception {
    explorationContext.setMetadataFor("number", JsonNodeFactory.instance.numberNode(1000));
    explorationContext.setMetadataFor("number", JsonNodeFactory.instance.numberNode(42));
    Optional<JsonNode> optionalNumber = explorationContext.getMetadataFor("number");
    assertTrue(optionalNumber.isPresent());
    assertThat("The number metadata must be persisted.", optionalNumber.get().asInt(),
        is(equalTo(42)));
    assertThat(explorationContext.getMetadataEntryNames(), contains("number"));
  }

  @Test
  public void test_putValuesData_mustBePersistent() throws Exception {
    ObjectNode aNode = JsonNodeFactory.instance.objectNode();
    aNode.set("dom", JsonNodeFactory.instance.numberNode(1.4));
    explorationContext.putValuesData("a", Arrays.asList("metrics", "relevance"), aNode);
    ObjectNode bNode = JsonNodeFactory.instance.objectNode();
    bNode.set("dom", JsonNodeFactory.instance.numberNode(2.2));
    explorationContext.putValuesData("b", Arrays.asList("metrics", "relevance"), bNode);

    assertThat(explorationContext.getResultIdsWithValues(), containsInAnyOrder("a", "b"));
    Optional<JsonNode> aValues = explorationContext
        .getValues("a", JsonPointer.compile("/metrics/relevance/dom"));
    assertTrue(aValues.isPresent());
    assertThat(aValues.get().asDouble(), is(1.4));
    Optional<JsonNode> bValues = explorationContext
        .getValues("b", JsonPointer.compile("/metrics/relevance/dom"));
    assertTrue(bValues.isPresent());
    assertThat(bValues.get().asDouble(), is(2.2));
  }

  @Test
  public void test_getValuesDataOfRoot_mustReturnAllValues() throws Exception {
    explorationContext.putValuesData("a", Collections.singletonList("centrality"), centralityObj);
    explorationContext.putValuesData("a", Collections.singletonList("similarity"), similarityObj);
    Optional<JsonNode> aValues = explorationContext.getValues("a");
    assertTrue(aValues.isPresent());
    List<String> fields = new LinkedList<>();
    aValues.get().fieldNames().forEachRemaining(fields::add);
    assertThat(fields, containsInAnyOrder("centrality", "similarity"));
  }

  @Test
  public void test_putValuesDataWithPtr_mustBePersistent() throws Exception {
    ObjectNode aNode = JsonNodeFactory.instance.objectNode();
    aNode.set("dom", JsonNodeFactory.instance.numberNode(1.4));
    explorationContext.putValuesData("a", JsonPointer.compile("/metrics/relevance"), aNode);
    ObjectNode bNode = JsonNodeFactory.instance.objectNode();
    bNode.set("dom", JsonNodeFactory.instance.numberNode(2.2));
    explorationContext.putValuesData("b", JsonPointer.compile("/metrics/relevance"), bNode);

    assertThat(explorationContext.getResultIdsWithValues(), containsInAnyOrder("a", "b"));
    Optional<JsonNode> aValues = explorationContext
        .getValues("a", JsonPointer.compile("/metrics/relevance/dom"));
    assertTrue(aValues.isPresent());
    assertThat(aValues.get().asDouble(), is(1.4));
    Optional<JsonNode> bValues = explorationContext
        .getValues("b", JsonPointer.compile("/metrics/relevance/dom"));
    assertTrue(bValues.isPresent());
    assertThat(bValues.get().asDouble(), is(2.2));
  }

  @Test
  public void test_getValuesDataWithRootPtr_mustReturnAllValues() throws Exception {
    explorationContext.putValuesData("a", JsonPointer.compile("/centrality"), centralityObj);
    explorationContext.putValuesData("a", JsonPointer.compile("/similarity"), similarityObj);
    Optional<JsonNode> aValues = explorationContext.getValues("a", JsonPointer.compile(""));
    assertTrue(aValues.isPresent());
    List<String> fields = new LinkedList<>();
    aValues.get().fieldNames().forEachRemaining(fields::add);
    assertThat(fields, containsInAnyOrder("centrality", "similarity"));
  }

  @Test
  public void test_getAllValues_mustReturnDeepCopy() throws Exception {
    explorationContext.putValuesData("a", JsonPointer.compile("/centrality"), centralityObj);
    explorationContext.putValuesData("a", JsonPointer.compile("/similarity"), similarityObj);
    Map<String, ObjectNode> allValues = explorationContext.getAllValues();
    explorationContext.putValuesData("a", JsonPointer.compile("/centrality/load"),
        JsonNodeFactory.instance.numberNode(121.42));
    assertThat(allValues.keySet(), containsInAnyOrder("a"));
    assertThat(allValues.get("a").get("centrality").get("load").asDouble(), is(equalTo(2.1)));
    ((ObjectNode) allValues.get("a").get("centrality")).put("pageRank", 100.0);
    assertThat(explorationContext.getValues("a", JsonPointer.compile("/centrality/pageRank")).get()
        .asDouble(), is(equalTo(4.5)));
  }

  @Test
  public void test_pushDataToRootPtr_mustBePersisted() throws Exception {
    assertTrue(ExplorationContext.ROOT_PTR.matches());
    ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
    rootNode.set("centrality", centralityObj);
    rootNode.set("similarity", similarityObj);
    explorationContext.putValuesData("a", ExplorationContext.ROOT_PTR, rootNode);

    Optional<JsonNode> aValues = explorationContext.getValues("a");
    assertTrue(aValues.isPresent());
    List<String> fields = new LinkedList<>();
    aValues.get().fieldNames().forEachRemaining(fields::add);
    assertThat(fields, containsInAnyOrder("centrality", "similarity"));
    assertThat(aValues.get().at(JsonPointer.compile("/centrality/acs")).asDouble(),
        is(equalTo(1.4)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_pushNumberToRoot_mustThrowIllegalArgumentException() throws Exception {
    explorationContext
        .putValuesData("a", ExplorationContext.ROOT_PTR, JsonNodeFactory.instance.numberNode(1));
  }
}
