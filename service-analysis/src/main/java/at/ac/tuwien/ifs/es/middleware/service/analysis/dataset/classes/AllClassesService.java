package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.Set;

/**
 * Instances get this service provide the ability to get information about the classes in the
 * knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://en.wikipedia.org/wiki/Lowest_common_ancestor">Least Common Subsumer</a>
 * @since 1.0
 */
public interface AllClassesService extends AnalysisService {

  /**
   * Gets all the classes in the knowledge graph.
   *
   * @return all the classes in the knowledge graph.
   */
  Set<Resource> getAllClasses();

}
