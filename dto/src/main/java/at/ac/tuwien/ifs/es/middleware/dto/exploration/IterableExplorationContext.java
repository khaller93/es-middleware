package at.ac.tuwien.ifs.es.middleware.dto.exploration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Instances of this interface are an {@link ExplorationContext} for which it is possible to iterate
 * sequentially over a list of results.
 *
 * @param <T> the type of the result for this {@link ExplorationContext}.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface IterableExplorationContext<T> extends Iterable<T> {


}
