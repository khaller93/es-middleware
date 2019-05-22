package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.neighbourhood.RDFTerm;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Instances of this interface allow one to iterate over the contained predicates.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface IterableObjectsContext<T extends IdentifiableResult> extends ExplorationContext<T> {

  /**
   * Gets an iterator with which it is possible to iterate over all predicates in the context.
   *
   * @return an iterator with which it is possible to iterate over all predicates in the context.
   */
  @JsonIgnore
  Iterator<RDFTerm> getObjectIterator();

  /**
   * Gets the predicates contained in the context as a {@link List} potentially with duplicates.
   *
   * @return a list of predicates (see {@link Resource}), which potentially has duplicates.
   */
  List<RDFTerm> asObjectList();

  /**
   * Gets the predicates contained in the context as a {@link Set} without duplicates.
   *
   * @return a set of predicates (see {@link Resource}) without duplicates.
   */
  Set<RDFTerm> asObjectSet();

}
