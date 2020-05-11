package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.Optional;

/**
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface PropertyCountService extends AnalysisService {

  /**
   * Gets the number of relationships with the given {@code property} in the knowledge graph.
   *
   * @param property for which the count shall be returned.
   * @return the number of relationships the given {@code property}, or {@link Optional#empty()} if
   * the number is not available.
   */
  Optional<Long> getCountOf(Resource property);

}
