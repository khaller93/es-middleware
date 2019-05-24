package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A value box helps to store data under a given name. It allows to manipulate the stored JSON data
 * in a flexible way.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(use = Id.NONE, defaultImpl = ValueBoxImpl.class)
public interface ValueBox {

  @JsonIgnore
  JsonPointer ROOT_PTR = JsonPointer.compile("");

  /**
   * Gets a {@link List} of all names under which a data node is stored.
   *
   * @return a {@link List} of all names under which a data node is stored.
   */
  Collection<String> names();

  /**
   * Gets a map representing this value box.
   *
   * @return a map representing this value box.
   */
  Map<String, JsonNode> asMap();

  /**
   * Gets a deep copy of this {@link ValueBox}.
   *
   * @return a deep copy of this {@link ValueBox}.
   */
  ValueBox deepCopy();

  /**
   * Puts the given {@code data}, which can be everything that is {@link Serializable}, as value
   * under the given {@code name}. Already existing data for this name will be overwritten.
   *
   * @param name to which the given {@code data} shall be stored. It must not be null or an empty
   * string.
   * @param data which shall be stored under the given {@code name}. It must not be null.
   */
  void put(String name, JsonNode data);

  /**
   * Puts the given {@code data} to the value node with the given {@code id} on the given position.
   *
   * @param name of the data node that shall be changed.
   * @param path position to which the data shall be stored in the data node. It must not be null,
   * but can be empty. Empty path list refers to the root.
   * @param data the data that shall be stored.
   */
  void put(String name, JsonPointer path, JsonNode data);

  /**
   * Merges the given value box with this value box. It keeps the given value box unchanged.
   *
   * @param valueBox that shall be merged into this value box. It must not be null.
   * @return a new {@link ValueBox} that is the merge of this value box with the given one.
   */
  ValueBox merge(ValueBox valueBox);

  /**
   * Gets the root {@link JsonNode} storing values for the given {@code name}. If there is no data
   * for {@code node}, {@link Optional#empty()} will be returned.
   *
   * @param name the name for which the root {@link JsonNode} shall be returned.
   * @return {@link JsonNode} on the given position in the value node stored under the given {@code
   * name}, or {@link Optional#empty()}, if there is no such data.
   */
  @JsonIgnore
  Optional<JsonNode> get(String name);

  /**
   * Gets the {@link JsonNode} on the given position in the data node stored under the given {@code
   * name}. If there is no data for {@code name} or the path goes nowhere, {@link Optional#empty()}
   * will be returned.
   *
   * @param name of the data node from which data shall be returned.
   * @param pointer position of the data that shall be returned. It must not be null.
   * @return {@link JsonNode} on the given position in the data node stored under the given {@code
   * name}, or {@link Optional#empty()}, if there is no such data.
   */
  Optional<JsonNode> get(String name, JsonPointer pointer);

  /**
   * Removes the data stored under the given {@code name}.
   *
   * @param name for which the stored data shall be removed. It must not be null or empty.
   */
  void remove(String name);

  /**
   * Clears the value box.
   */
  void clear();

}
