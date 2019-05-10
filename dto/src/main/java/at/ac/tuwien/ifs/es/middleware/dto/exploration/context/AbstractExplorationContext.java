package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * This class implements universal methods of an {@link ExplorationContext} such that the more
 * specific implementation do not have to repeat themselves.
 *
 * @param <T> the type of the result for this {@link ExplorationContext}, which must be
 * identifiable.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractExplorationContext<T extends IdentifiableResult> implements
    ExplorationContext<T> {

  @JsonProperty
  private Map<String, ObjectNode> values;
  @JsonProperty
  private Map<String, JsonNode> metadata;

  protected AbstractExplorationContext() {
    this.values = new HashMap<>();
    this.metadata = new HashMap<>();
  }

  protected AbstractExplorationContext(Map<String, ObjectNode> values,
      Map<String, JsonNode> metadata) {
    checkArgument(values != null, "The passed values map can be empty, but must not be null.");
    checkArgument(metadata != null, "The passed metadata map can be empty, but must not be null.");
    this.values = values;
    this.metadata = metadata;
  }

  @Override
  public Map<String, ObjectNode> getAllValues() {
    Map<String, ObjectNode> copiedValues = new HashMap<>();
    for (String id : values.keySet()) {
      copiedValues.put(id, values.get(id).deepCopy());
    }
    return copiedValues;
  }

  @Override
  public Map<String, JsonNode> getMetadata() {
    Map<String, JsonNode> copiedMetadata = new HashMap<>();
    for (String name : metadata.keySet()) {
      copiedMetadata.put(name, metadata.get(name).deepCopy());
    }
    return copiedMetadata;
  }

  @Override
  public void putValuesData(String id, List<String> path, JsonNode data) {
    if (path == null || path.isEmpty()) {
      if (data.isObject()) {
        values.put(id, (ObjectNode) data);
      } else {
        throw new IllegalArgumentException("Root node must be an object, and not a value node.");
      }
    } else {
      ObjectNode node = values
          .compute(id, (s, nodes) -> nodes != null && !nodes.isMissingNode() ? nodes
              : JsonNodeFactory.instance.objectNode());
      for (String segmentName : path.subList(0, path.size() - 1)) {
        node = pushNode(segmentName, node);
      }
      node.set(path.get(path.size() - 1), data);
    }
  }

  @Override
  public void clearValues() {
    values.clear();
  }

  @Override
  public void putValuesData(String id, JsonPointer path, JsonNode data) {
    checkArgument(id != null && !id.isEmpty(), "The id must not be null or empty.");
    checkArgument(path != null, "The path must not be null.");
    checkArgument(data != null, "The data node to push must not be null.");
    if (path.matches()) {
      if (data.isObject()) {
        values.put(id, (ObjectNode) data);
      } else {
        throw new IllegalArgumentException("Root node must be an object, and not a value node.");
      }
    } else {
      ObjectNode node = values
          .compute(id, (s, nodes) -> nodes != null && !nodes.isMissingNode() ? nodes
              : JsonNodeFactory.instance.objectNode());
      while (!path.tail().matches()) {
        node = pushNode(path.getMatchingProperty(), node);
        path = path.tail();
      }
      node.set(path.getMatchingProperty(), data);
    }
  }

  private ObjectNode pushNode(String segmentName, ObjectNode node) {
    if (node.has(segmentName)) {
      JsonNode newNode = node.get(segmentName);
      if (newNode.isObject()) {
        return (ObjectNode) newNode;
      } else {
        throw new IllegalArgumentException(
            "There is a primitive value stored between the given path.");
      }
    } else {
      return node.putObject(segmentName);
    }
  }

  @Override
  public void removeValuesData(String id) {
    checkArgument(id != null && !id.isEmpty(), "The given id must not be null or empty.");
    values.remove(id);
  }

  @Override
  public Set<String> getResultIdsWithValues() {
    return values.keySet();
  }

  @Override
  public Optional<JsonNode> getValues(String id, JsonPointer path) {
    checkArgument(id != null && !id.isEmpty(),
        "The id for getting value entries must not be null or empty.");
    checkArgument(path != null, "The given path must not be null.");
    ObjectNode valuesNode = values.get(id);
    if (valuesNode != null) {
      JsonNode value = valuesNode.at(path);
      return value.isMissingNode() ? Optional.empty() : Optional.of(value);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void mergeValues(Map<String, ObjectNode> valuesMap) {
    checkArgument(valuesMap != null, "The value map to merge with must not be null.");
    /* make a deep copy */
    Map<String, ObjectNode> copiedValues = new HashMap<>();
    for (String id : values.keySet()) {
      copiedValues.put(id, values.get(id).deepCopy());
    }
    /* compute the merge */
    for (Entry<String, ObjectNode> valueEntry : valuesMap.entrySet()) {
      if (copiedValues.containsKey(valueEntry.getKey())) {
        copiedValues.put(valueEntry.getKey(),
            (ObjectNode) mergeNodes(copiedValues.get(valueEntry.getKey()), valueEntry.getValue()));
      } else {
        copiedValues.put(valueEntry.getKey(), valueEntry.getValue());
      }
    }
    /* set the merged values map */
    this.values = copiedValues;
  }

  @Override
  public void mergeMetadata(Map<String, JsonNode> metadataMap) {
    checkArgument(metadataMap != null, "The metadata map to merge with must not be null.");
    /* make a deep copy */
    Map<String, JsonNode> metadataValues = new HashMap<>();
    for (String id : metadata.keySet()) {
      metadataValues.put(id, metadata.get(id).deepCopy());
    }
    /* compute the merge */
    for (Entry<String, JsonNode> metadataEntry : metadataMap.entrySet()) {
      if (metadataValues.containsKey(metadataEntry.getKey())) {
        metadataValues.put(metadataEntry.getKey(),
            mergeNodes(metadataValues.get(metadataEntry.getKey()),
                metadataEntry.getValue()));
      } else {
        metadataValues.put(metadataEntry.getKey(), metadataEntry.getValue());
      }
    }
    /* set the merged values map */
    this.metadata = metadataValues;
  }

  /**
   * Merges the given source {@link JsonNode} and {@code toMerge} {@link JsonNode}. Later has
   * precedence. Value, array and null nodes are considered as atomic and are not merged, only
   * object nodes.
   *
   * @param source {@link JsonNode} that is the source node for merging.
   * @param toMerge {@link JsonNode} that is the node to merge into the source.
   */
  private JsonNode mergeNodes(JsonNode source, JsonNode toMerge) {
    if (toMerge.isObject()) {
      if (source.isObject()) {
        ObjectNode sourceObj = ((ObjectNode) source);
        ObjectNode toMergeObj = ((ObjectNode) toMerge);
        Iterator<Entry<String, JsonNode>> fieldIterator = toMergeObj.fields();
        while (fieldIterator.hasNext()) {
          Entry<String, JsonNode> fieldEntry = fieldIterator.next();
          if (sourceObj.has(fieldEntry.getKey())) {
            sourceObj.replace(fieldEntry.getKey(),
                mergeNodes(sourceObj.get(fieldEntry.getKey()), fieldEntry.getValue()));
          } else {
            sourceObj.set(fieldEntry.getKey(), fieldEntry.getValue());
          }
        }
        return sourceObj;
      } else {
        return toMerge;
      }
    } else {
      return toMerge;
    }
  }

  @Override
  public void clearMetadata() {
    metadata.clear();
  }

  @Override
  public void setMetadataFor(String name, JsonNode data) {
    checkArgument(name != null && !name.isEmpty(), "The given name must not be null or empty.");
    checkArgument(data != null, "The data added to metadata must not be null.");
    metadata.put(name, data);
  }

  @Override
  public Optional<JsonNode> getMetadataFor(String name) {
    checkArgument(name != null && !name.isEmpty(), "The given name must not be null or empty.");
    JsonNode jsonNode = metadata.get(name);
    if (jsonNode != null) {
      return Optional.of(jsonNode);
    }
    return Optional.empty();
  }

  @Override
  public Set<String> getMetadataEntryNames() {
    return metadata.keySet();
  }

  @Override
  public void removeMetadataFor(String name) {
    checkArgument(name != null && !name.isEmpty(), "The given name must not be null or empty.");
    metadata.remove(name);
  }
}
