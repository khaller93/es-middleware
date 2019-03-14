package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.Set;

/**
 * This service provides methods to access the class hierarchy in the knowledge graph.
 *
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
   * @throws IllegalArgumentException if the given class list is null.
   */
  Set<Resource> getAllClasses(Set<Resource> classes);

  /**
   * Returns the most specific classes of the given list.
   *
   * @param classes a list of classes as {@link Resource}s. It must not be null.
   * @return the most specific classes of the given list.
   * @throws IllegalArgumentException if the given class list is null.
   */
  Set<Resource> getMostSpecificClasses(Set<Resource> classes);

  /**
   * Gets all the parent classes of the given class.
   *
   * @param classResource for which the parent classes shall be returned.  It must not be null.
   * @return a {@link Set} with all the parent class {@link Resource}s. {@code null} must not be
   * returned.
   * @throws IllegalArgumentException if the given class resource is null.
   */
  Set<Resource> getParentClasses(Resource classResource);

  /**
   * The set of Lowest Common Ancestors (LCA) of two classes {@code classA} and {@code classB} in a
   * class hierarchy is a set of classes {@code C} such that all vertices in {@code C} are common
   * ancestors of {@code classA} and {@code classB} and no other descendant of the vertices in
   * {@code C} is an ancestor of {@code classA} and {@code classB}.
   *
   * @param classA class {@link Resource}, must not be null.
   * @param classB class {@link Resource}, must not be null.
   * @return the set of classes that are the lowest common ancestors of the given class pair.
   * @throws IllegalArgumentException if one of the given class resources is null.
   */
  Set<Resource> getLowestCommonAncestor(Resource classA, Resource classB);
}
