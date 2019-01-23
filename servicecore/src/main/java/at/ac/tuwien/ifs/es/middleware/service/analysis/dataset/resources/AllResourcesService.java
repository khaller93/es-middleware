package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.List;

/**
 * An implementation of {@link AnalysisService} that provides a method to fetch all resources in a
 * knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AllResourcesService extends AnalysisService {

  /**
   * Gets a list of all {@link Resource}s in the knowledge graph.
   *
   * @return a list of all {@link Resource}s in the knowledge graph.
   */
  List<Resource> getResourceList();

}
