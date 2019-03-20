package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.Optional;
import java.util.Set;

/**
 * This service maintains the classes with their corresponding resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface ClassResourceService extends AnalysisService {

  /**
   * Gets instances of given class {@link Resource}.
   *
   * @param classResource class {@link Resource}. It must not be null.
   * @return a set of resources that are memmber of the given {@code classResource}. {@link
   * Optional#empty()}, if no information is available for the given class.
   */
  Optional<Set<Resource>> getInstancesOfClass(Resource classResource);

}
