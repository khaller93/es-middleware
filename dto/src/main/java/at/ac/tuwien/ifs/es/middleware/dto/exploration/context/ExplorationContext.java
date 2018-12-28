package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Instances of this interface represent an intermediate or final result of an exploration flow.
 *
 * @param <T> the type of the result for this {@link ExplorationContext}, which must be
 * identifiable.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.MINIMAL_CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class")
public interface ExplorationContext<T extends IdentifiableResult> extends Iterable<T>,
    Collector<T, ExplorationContextContainer<T>, ExplorationContext<T>> {

  @JsonIgnore
  JsonPointer ROOT_PTR = JsonPointer.compile("");

  /**
   * Gets a {@link Stream} of the results of this {@link ExplorationContext}.
   *
   * @return a {@link Stream} of the results of this {@link ExplorationContext}.
   */
  Stream<T> streamOfResults();

  /**
   * Sets the given {@code data}, which can be everything that is {@link Serializable}, as value of
   * the given {@code name} in the metadata map. Already existing data for this name will be
   * overwritten.
   *
   * @param name to which the given {@code data} shall be stored.
   * @param data which shall be stored under the given {@code name}.
   */
  @JsonIgnore
  void setMetadataFor(String name, JsonNode data);

  /**
   * Removes metadata stored under the given name.
   *
   * @param name of which the metadata shall be removed.
   */
  void removeMetadataFor(String name);

  /**
   * Gets the metadata stored under the given {@code name}.
   *
   * @param name under which the demanded metadata is stored.
   */
  Optional<JsonNode> getMetadataFor(String name);

  /**
   * Gets a {@link List} of names under which metadata has been stored.
   *
   * @return a {@link List} of names under which metadata has been stored.
   */
  @JsonIgnore
  Set<String> getMetadataEntryNames();

  /**
   * Gets a deep copy of the metadata stored by this context.
   *
   * @return a deep copy of the metadata stored by this context.
   */
  @JsonIgnore
  Map<String, JsonNode> getMetadata();

  /**
   * Puts the given {@code data} to the value node with the given {@code id} on the given position.
   * The data can then be accessed with {@link ExplorationContext#getValues(String, JsonPointer)}.
   *
   * @param id to which the data shall be stored.
   * @param path position to which the data shall be stored.
   * @param data the data that shall be stored.
   */
  void putValuesData(String id, List<String> path, JsonNode data);

  /**
   * Puts the given {@code data} to the value node with the given {@code id} on the given position.
   * The data can then be accessed with {@link ExplorationContext#getValues(String, JsonPointer)}.
   *
   * @param id to which the data shall be stored.
   * @param path position to which the data shall be stored.
   * @param data the data that shall be stored.
   */
  void putValuesData(String id, JsonPointer path, JsonNode data);

  /**
   * Gets the {@link JsonNode} on the given position in the value node stored under the given {@code
   * id}. If there is no data for {@code id} or the path goes nowhere, {@link Optional#EMPTY} will
   * be returned.
   *
   * @param id the id for which the {@link JsonNode} on the given position shall be returned.
   * @param path refers to the data in the {@link JsonNode}.
   * @return {@link JsonNode} on the given position in the value node stored under the given {@code
   * id}, or {@link Optional#EMPTY}, if there is no such data.
   */
  Optional<JsonNode> getValues(String id, JsonPointer path);

  /**
   * Gets the root {@link JsonNode} storing values for the given {@code id}. If there is no data for
   * {@code id}, {@link Optional#EMPTY} will be returned.
   *
   * @param id the id for which the root {@link JsonNode} shall be returned.
   * @return {@link JsonNode} on the given position in the value node stored under the given {@code
   * id}, or {@link Optional#EMPTY}, if there is no such data.
   */
  default Optional<JsonNode> getValues(String id) {
    return getValues(id, ROOT_PTR);
  }

  /**
   * Gets a deep copy of the values stored by this context.
   *
   * @return a deep copy of the values stored by this context.
   */
  @JsonIgnore
  Map<String, ObjectNode> getAllValues();

  /**
   * Removes the data for the given {@code id}.
   *
   * @param id for which data shall be removed.
   */
  void removeValuesData(String id);

  /**
   * Gets a {@link List} of all result ids for which values are stored in the context.
   *
   * @return a {@link List} of all result ids for which values are stored in the context.
   */
  @JsonIgnore
  Set<String> getResultIdsWithValues();

}
