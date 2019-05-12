package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Instance of this interface represent a {@link ExplorationContext} that is a collection of {@link
 * IdentifiableResult}s of a certain type.
 *
 * @param <T> the type of the {@link IdentifiableResult}.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface ResultCollectionContext<T extends IdentifiableResult> extends
    ExplorationContext<T>, Iterable<T> {

  /**
   * Gets a {@link Stream} of the results of this {@link ExplorationContext}.
   *
   * @return a {@link Stream} of the results of this {@link ExplorationContext}.
   */
  Stream<T> streamOfResults();

  /**
   * Gets a {@link Collector} for this context that
   *
   * @return a {@link Collector} for this context.
   */
  Collector<T, ? extends ExplorationContextContainer<T>, ? extends ResultCollectionContext<T>> collector();

  /**
   * Adds the given {@code entry} to this context.
   *
   * @param entry that shall be added. It must not be null.
   */
  void add(T entry);

  /**
   * Removes the given {@code entry} from this context.
   *
   * @param entry that shall be removed from this context. It must not be null.
   */
  void remove(T entry);

  /**
   * Gets the size of the collection. It can be a positive number including 0.
   *
   * @return the size of the collection. It can be a positive number including 0.
   */
  long size();

}
