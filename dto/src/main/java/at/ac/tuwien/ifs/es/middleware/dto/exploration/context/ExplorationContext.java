package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
public interface ExplorationContext<T extends IdentifiableResult> extends Iterable<T> {

  @JsonIgnore
  Collection<T> getResultsCollection();

  void setResults(Collection<T> results);

  void removeResult(T result);

  /**
   * Sets the given {@code data}, which can be everything that is {@link Serializable}, as value of
   * the given {@code name} in the metadata map. Already existing data for this name will be
   * overwritten.
   *
   * @param name to which the given {@code data} shall be stored.
   * @param data which shall be stored under the given {@code name}.
   */
  void setMetadata(String name, Serializable data);

  /**
   * Removes metadata stored under the given name.
   *
   * @param name of which the metadata shall be removed.
   */
  void removeMetadata(String name);

  /**
   * Puts the given {@code data} to the value node with the given {@code id} on the given position.
   * The data can then be accessed with {@link ExplorationContext#get(String, List)}.
   *
   * @param id to which the data shall be stored.
   * @param path position to which the data shall be stored.
   * @param data the data that shall be stored.
   */
  void putValuesData(String id, List<String> path, JsonNode data);

  /**
   * Removes the data for the given {@code id}.
   *
   * @param id for which data shall be removed.
   */
  void removeValuesData(String id);

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
  Optional<JsonNode> get(String id, List<String> path);
}
