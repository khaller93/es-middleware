package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.Optional;
import java.util.Set;

/**
 * This service maintains the classes an instance is member of.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface ResourceClassService extends AnalysisService {

  /**
   * Gets all the class {@link Resource}s for the given instance.
   *
   * @param instance for which all the class {@link Resource}s (it is a member of) shall be
   * returned. It must not be null.
   * @return {@link Set} of class {@link Resource}s that the given instance is a member of.
   */
  Optional<Set<Resource>> getClassesOf(Resource instance);
}
