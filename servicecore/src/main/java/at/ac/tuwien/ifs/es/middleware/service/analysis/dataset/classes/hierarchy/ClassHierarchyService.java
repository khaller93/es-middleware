package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.Set;

/**
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface ClassHierarchyService extends AnalysisService {

  /**
   * Returns the most specific classes of the given list.
   *
   * @param classes a list of classes as {@link Resource}s. It must not be null.
   * @return the most specific classes of the given list.
   */
  Set<Resource> getMostSpecificClasses(Set<Resource> classes);

}
