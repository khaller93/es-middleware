package at.ac.tuwien.ifs.es.middleware.service.exploration.context;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Identifiable;

/**
 *
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface SortableContext<T extends Identifiable> {

  void sort();

}
