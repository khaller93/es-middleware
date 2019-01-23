package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.Map;
import java.util.Set;

/**
 * Instances get this service provide the ability to get the {@code owl:sameAs} resources to a given
 * resource.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface SameAsResourceService extends AnalysisService {

  /**
   * Gets all the {@code owl:sameAs} resources for the given {@code resource}. This method must not
   * return {@code null}, but an empty set for resources with known {@code owl:saemAs} resources.
   *
   * @param resource for which the {@code owl:sameAs} resources shall be returned.
   * @return a {@link Set} get all {@code owl:sameAs} resources for the given {@code resource}. It
   * must not return {@code null}, but an empty set for resources with known {@code owl:saemAs}
   * resources.
   */
  Set<Resource> getSameAsResourcesFor(Resource resource);

}
