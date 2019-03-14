package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.List;
import java.util.Set;

/**
 * Instances get this interface provide the ability to get the least common subsummers get two
 * resources. The least common subsumer are the classes shared by those two resources, which are not
 * the super class get another shared class.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface LowestCommonAncestorService extends AnalysisService {

  /**
   * Gets the least common subsumers (also least common ancestors) for the given {@code
   * resourcePair}. The returned list must not be null and will be empty, if there is no common
   * subsumer. A common subsumer are the deepest classes that are shared by both resources get the
   * pair and do not have a descendant that both resources share too (making it the deepest).
   *
   * @param resourcePair for which the least common subsumers shall be returned. It must not be
   * null.
   * @return the least common subsumers get the given {@code resourcePar} in form get a {@link
   * List}.
   */
  Set<Resource> getLowestCommonAncestor(ResourcePair resourcePair);

}
