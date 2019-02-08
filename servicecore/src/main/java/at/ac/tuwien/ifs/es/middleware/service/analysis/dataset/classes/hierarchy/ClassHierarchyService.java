package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.Set;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface ClassHierarchyService extends AnalysisService {

  /**
   * Gets all the parent classes for each of given classes and returns a set that contains the given
   * classes with all the collected parent classes.
   *
   * @param classes for which all the classes shall be returned.  It must not be null.
   * @return a {@link Set} of all class {@link Resource}s.
   */
  Set<Resource> getAllClasses(Set<Resource> classes);

  /**
   * Returns the most specific classes of the given list.
   *
   * @param classes a list of classes as {@link Resource}s. It must not be null.
   * @return the most specific classes of the given list.
   */
  Set<Resource> getMostSpecificClasses(Set<Resource> classes);

  /**
   * Gets all the parent classes of the given class.
   *
   * @param classResource for which the parent classes shall be returned.  It must not be null.
   * @return a {@link Set} with all the parent class {@link Resource}s. {@code null} must not be
   * returned.
   */
  Set<Resource> getParentClasses(Resource classResource);

  /**
   *
   *
   * @return
   */
  Set<Resource> getLeastCommonAncestor(Resource classA, Resource classB);
}
