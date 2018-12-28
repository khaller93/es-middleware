package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the generic methods for taking track of the results collected during a
 * {@link java.util.stream.Collector} action of {@link ExplorationContext}.
 *
 * @param <T> of the result that shall be contained during the collection.
 */
public abstract class ExplorationContextContainer<T extends IdentifiableResult> {

  private Map<String, ObjectNode> originalValuesMap;
  private Map<String, JsonNode> metadata;

  private Collection<T> resultCollection;
  private Map<String, ObjectNode> valuesMap;

  protected ExplorationContextContainer(Map<String, ObjectNode> originalValuesMap,
      Map<String, JsonNode> metadata, Collection<T> resultCollection) {
    this.originalValuesMap = originalValuesMap;
    this.metadata = metadata;
    this.resultCollection = resultCollection;
    this.valuesMap = new HashMap<>();
  }

  /**
   * Gets a {@link Collection} of results maintained by this container.
   *
   * @return a {@link Collection} of results maintained by this container.
   */
  public Collection<T> getResultCollection() {
    return resultCollection;
  }

  public void addResult(T result) {
    resultCollection.add(result);
    valuesMap.putAll(getValuesOf(result, originalValuesMap));
  }

  /**
   * Takes a look at the passed original {@link Map} of values and extracts the relevant values for
   * the given {@code result}. Those values are then returned in a sub map.
   *
   * @param result for which the corresponding values shall be returned.
   * @param originalValuesMap the map of original values from which relevant values shall be
   * extracted.
   * @return a map of values for the given {@code result}.
   */
  protected abstract Map<String, ObjectNode> getValuesOf(T result,
      Map<String, ObjectNode> originalValuesMap);

  /**
   * Gets the meta data maintained by this container.
   *
   * @return the meta data maintained by this container.
   */
  public Map<String, JsonNode> getMetadata() {
    return metadata;
  }

  /**
   * Gets the current values map maintained by this container.
   *
   * @return the current values map maintained by this container.
   */
  public Map<String, ObjectNode> getValuesMap() {
    return valuesMap;
  }
}
