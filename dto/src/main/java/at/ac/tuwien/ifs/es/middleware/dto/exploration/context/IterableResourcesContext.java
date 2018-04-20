package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * Instances of this interface allow to iterate over the containing resources (IRI or blank node).
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface IterableResourcesContext {

  /**
   * Gets an iterator with which it is possible to iterate over all resources in the context.
   *
   * @return an iterator with which it is possible to iterate over all resources in the context.
   */
  @JsonIgnore
  Iterator<Resource> getResourceIterator();

  /**
   * Gets the iterable resources as a {@link List} potentially with duplicates.
   *
   * @return a list of {@link BlankNodeOrIRI}, which potentially has duplicates.
   */
  List<Resource> asResourceList();

  /**
   * Gets the iterable resources as a {@link Set} without duplicates.
   *
   * @return a set of {@link BlankNodeOrIRI} without duplicates.
   */
  Set<Resource> asResourceSet();

}
