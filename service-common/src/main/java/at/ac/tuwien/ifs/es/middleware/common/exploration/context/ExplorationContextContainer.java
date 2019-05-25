package at.ac.tuwien.ifs.es.middleware.common.exploration.context;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.box.ValueBox;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.box.ValueBoxFactory;
import java.util.Collection;
import java.util.Map;

/**
 * This class implements the generic methods for taking track of the results collected during a
 * {@link java.util.stream.Collector} action of {@link ExplorationContext}.
 *
 * @param <T> of the result that shall be contained during the collection.
 */
public abstract class ExplorationContextContainer<T extends IdentifiableResult> {

  private ValueBox originalValuesMap;
  private ValueBox metadata;

  private Collection<T> resultCollection;
  private ValueBox valuesMap;

  public ExplorationContextContainer(Collection<T> resultCollection, ValueBox originalValuesMap,
      ValueBox metadata) {
    this.resultCollection = resultCollection;
    this.originalValuesMap = originalValuesMap;
    this.valuesMap = ValueBoxFactory.newBox();
    this.metadata = metadata;
  }

  /**
   * Gets a {@link Collection} of results maintained by this container.
   *
   * @return a {@link Collection} of results maintained by this container.
   */
  public Collection<T> getResultCollection() {
    return resultCollection;
  }

  /**
   * Adds a new result into this container.
   *
   * @param result that shall be added to this container. It must not be null.
   */
  public void addResult(T result) {
    checkArgument(result != null, "The given result must not be null.");
    resultCollection.add(result);
    valuesMap.merge(getValuesOf(result, originalValuesMap));
  }

  /**
   * Gets the current values map maintained by this container.
   *
   * @return the current values map maintained by this container.
   */
  public ValueBox getValues() {
    return valuesMap;
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
  protected abstract ValueBox getValuesOf(T result, ValueBox originalValuesMap);

  /**
   * Gets the meta data maintained by this container.
   *
   * @return the meta data maintained by this container.
   */
  public ValueBox getMetadata() {
    return metadata;
  }


}
