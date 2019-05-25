package at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.box;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * This class is an implementation of {@link ValueBox} that uses a {@link JsonNode} for maintaining
 * values.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonSerialize(using = ValueBoxImplJsonTranscoder.Serializer.class)
@JsonDeserialize(using = ValueBoxImplJsonTranscoder.Deserializer.class)
public class ValueBoxImpl implements ValueBox {

  @JsonProperty("box")
  private Map<String, JsonNode> box;

  public ValueBoxImpl() {
    this(Collections.emptyMap());
  }

  @JsonCreator
  public ValueBoxImpl(@JsonProperty(value = "box", required = true) Map<String, JsonNode> box) {
    checkArgument(box != null, "The given value box must not be null.");
    this.box = new HashMap<>(box);
  }

  @Override
  public Collection<String> names() {
    return box.keySet();
  }

  @Override
  public Map<String, JsonNode> asMap() {
    Map<String, JsonNode> map = new HashMap<>();
    for (Entry<String, JsonNode> entry : box.entrySet()) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }

  @Override
  public ValueBox deepCopy() {
    return new ValueBoxImpl(asMap());
  }

  @Override
  public void put(String name, JsonNode data) {
    checkArgument(name != null && !name.isEmpty(), "The name must not be null or empty.");
    checkArgument(data != null, "The data must not be null.");

    box.put(name, data);
  }

  @Override
  public void put(String name, JsonPointer pointer, JsonNode data) {
    checkArgument(name != null && !name.isEmpty(), "The name must not be null or empty.");
    checkArgument(pointer != null, "The JSON pointer must not be null.");
    checkArgument(data != null, "The data must not be null.");

    if (pointer.matches()) {
      box.put(name, data);
    } else {
      ObjectNode node = (ObjectNode) box.compute(name, (pName, pJsonNode) -> {
        if (pJsonNode != null && pJsonNode.isObject()) {
          return pJsonNode;
        } else {
          return JsonNodeFactory.instance.objectNode();
        }
      });
      while (!pointer.tail().matches()) {
        node = pushNode(pointer.getMatchingProperty(), node);
        pointer = pointer.tail();
      }
      node.set(pointer.getMatchingProperty(), data);
    }
  }

  private ObjectNode pushNode(String segmentName, ObjectNode node) {
    if (node.has(segmentName)) {
      JsonNode newNode = node.get(segmentName);
      if (newNode.isObject()) {
        return (ObjectNode) newNode;
      } else {
        return node.putObject(segmentName);
      }
    } else {
      return node.putObject(segmentName);
    }
  }

  @Override
  public ValueBox merge(ValueBox otherValueBox) {
    checkArgument(otherValueBox != null, "The value box to merge with must not be null.");
    /* value box maps */
    Map<String, JsonNode> thisValueBoxMap = this.asMap();
    Map<String, JsonNode> otherValueBoxMap = otherValueBox.asMap();
    /* compute the merge */
    for (Entry<String, JsonNode> valueEntry : otherValueBoxMap.entrySet()) {
      if (thisValueBoxMap.containsKey(valueEntry.getKey())) {
        thisValueBoxMap.put(valueEntry.getKey(),
            mergeNodes(thisValueBoxMap.get(valueEntry.getKey()),
                valueEntry.getValue()));
      } else {
        thisValueBoxMap.put(valueEntry.getKey(), valueEntry.getValue());
      }
    }
    this.box = thisValueBoxMap;
    return new ValueBoxImpl(thisValueBoxMap);
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
  public Optional<JsonNode> get(String name) {
    checkArgument(name != null && !name.isEmpty(), "The name must not be null or empty.");

    JsonNode node = box.get(name);
    if (node != null) {
      return Optional.of(node);
    }
    return Optional.empty();
  }

  @Override
  public Optional<JsonNode> get(String name, JsonPointer pointer) {
    checkArgument(name != null && !name.isEmpty(), "The name must not be null or empty.");
    checkArgument(pointer != null, "The JSON pointer must not be null.");

    JsonNode valuesNode = box.get(name);
    if (valuesNode != null) {
      JsonNode value = valuesNode.at(pointer);
      return value.isMissingNode() ? Optional.empty() : Optional.of(value);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void remove(String name) {
    checkArgument(name != null && !name.isEmpty(), "The name must not be null or empty.");

    box.remove(name);
  }

  @Override
  public void clear() {
    box.clear();
  }

}
