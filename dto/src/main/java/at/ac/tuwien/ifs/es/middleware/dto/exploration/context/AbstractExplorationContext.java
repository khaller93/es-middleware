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
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("The path must not be empty.");
    }
    ObjectNode node;
    if (!values.containsKey(id)) {
      node = JsonNodeFactory.instance.objectNode();
      values.put(id, node);
    } else {
      node = values.get(id);
    }
    putData(node, path, data);
  }

  private void putData(ObjectNode obj, List<String> path, JsonNode data) {
    if (path.size() == 1) {
      obj.set(path.get(0), data);
    } else {
      if (obj.has(path.get(0))) {
        JsonNode newNode = obj.get(path.get(0));
        if (newNode.isObject()) {
          putData((ObjectNode) newNode, path.subList(1, path.size()), data);
        } else {
          throw new IllegalArgumentException(
              "There is a primitive value stored between the given path.");
        }
      } else {
        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        obj.set(path.get(0), newNode);
        putData(newNode, path.subList(1, path.size()), data);
      }
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
