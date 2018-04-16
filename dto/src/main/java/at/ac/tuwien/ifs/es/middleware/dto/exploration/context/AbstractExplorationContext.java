package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractExplorationContext<T extends IdentifiableResult> implements
    ExplorationContext<T> {

  @JsonProperty
  private Map<String, ObjectNode> values = new HashMap<>();
  @JsonProperty
  private Map<String, Serializable> metadata = new HashMap<>();

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
  public Optional<JsonNode> get(String id, List<String> path) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("The path must not be empty.");
    }
    if (values.containsKey(id)) {
      JsonNode node = values.get(id);
      for (String field : path) {
        if (field.isEmpty()) {
          throw new IllegalArgumentException("The fields in the path must not be empty.");
        }
        if (node.isObject() && node.has(field)) {
          node = node.get(field);
        } else {
          return Optional.empty();
        }
      }
      return Optional.of(node);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void setMetadata(String name, Serializable data) {
    metadata.put(name, data);
  }


  @Override
  public void removeMetadata(String name) {
    metadata.remove(name);
  }

}
