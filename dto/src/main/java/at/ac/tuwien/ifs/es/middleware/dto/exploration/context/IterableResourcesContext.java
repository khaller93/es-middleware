package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Instances of this interface allow one to iterate over the contained resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface IterableResourcesContext<T extends IdentifiableResult> extends
    ExplorationContext<T> {

  /**
   * Gets an iterator with which it is possible to iterate over all resources in the context.
   *
   * @return an iterator with which it is possible to iterate over all resources in the context.
   */
  @JsonIgnore
  Iterator<Resource> getResourceIterator();

  /**
   * Gets the resources contained in this context as a {@link List} potentially with duplicates.
   *
   * @return a list of {@link Resource}, which potentially has duplicates.
   */
  List<Resource> asResourceList();

  /**
   * Gets the resources contained in this context as a {@link Set} without duplicates.
   *
   * @return a set of {@link Resource} without duplicates.
   */
  Set<Resource> asResourceSet();

}
