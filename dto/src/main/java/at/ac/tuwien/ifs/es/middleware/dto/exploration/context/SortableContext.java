package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

/**
 *
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface SortableContext<T extends IdentifiableResult> {

  void sort();

}
