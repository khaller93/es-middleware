package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    for (String name : copiedMetadata.keySet()) {
      copiedMetadata.put(name, metadata.get(name).deepCopy());
    }
    return copiedMetadata;
  }

  @Override
  public void putValuesData(String id, List<String> path, JsonNode data) {
    if (path.isEmpty()) {
      if (data.isObject()) {
        values.put(id, (ObjectNode) data);
      } else {
        throw new IllegalArgumentException("Root node must be an object, and not a value node.");
      }
    }
    ObjectNode node = values
        .compute(id, (s, nodes) -> nodes != null && !nodes.isMissingNode() ? nodes
            : JsonNodeFactory.instance.objectNode());
    for (String segmentName : path.subList(0, path.size() - 1)) {
      node = pushNode(segmentName, node);
    }
    node.set(path.get(path.size() - 1), data);
    //putData(node, path, data);
  }

  @Override
  public void putValuesData(String id, JsonPointer path, JsonNode data) {
    if (path.matches()) {
      if (data.isObject()) {
        values.put(id, (ObjectNode) data);
      } else {
        throw new IllegalArgumentException("Root node must be an object, and not a value node.");
      }
    }
    ObjectNode node = values
        .compute(id, (s, nodes) -> nodes != null && !nodes.isMissingNode() ? nodes
            : JsonNodeFactory.instance.objectNode());
    while (!path.tail().matches()) {
      node = pushNode(path.getMatchingProperty(), node);
      path = path.tail();
    }
    node.set(path.getMatchingProperty(), data);
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
    values.remove(id);
  }

  @Override
  public Set<String> getResultIdsWithValues() {
    return values.keySet();
  }

  @Override
  public Optional<JsonNode> getValues(String id, JsonPointer path) {
    ObjectNode valuesNode = values.get(id);
    if (valuesNode != null) {
      JsonNode value = valuesNode.at(path);
      return value.isMissingNode() ? Optional.empty() : Optional.of(value);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void setMetadataFor(String name, JsonNode data) {
    metadata.put(name, data);
  }

  @Override
  public Optional<JsonNode> getMetadataFor(String name) {
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
    metadata.remove(name);
  }
}
