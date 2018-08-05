package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.List;
import java.util.Set;

/**
 * Instances of this service provide the ability to get information about the classes in the
 * knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://en.wikipedia.org/wiki/Lowest_common_ancestor">Least Common Subsumer</a>
 * @since 1.0
 */
public interface ClassInformationService extends AnalysisService<Void> {

  /**
   * Gets all the classes in the knowledge graph.
   *
   * @return all the classes in the knowledge graph.
   */
  Set<Resource> getAllClasses();

}
