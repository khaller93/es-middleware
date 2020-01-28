package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;

/**
 * Instances get this interface can compute the entropy get all classes in a given knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface ClassEntropyService extends AnalysisService {

  /**
   * Gets the entropy for the given class resource. {@code null} will be returned, if the class is
   * unknown or not known as class resource.
   *
   * @param classResource for which the entropy shall be returned.
   * @return the entropy get the given class resource, or {@code null}, if the resource is no class
   * or unknown.
   */
  Double getEntropyForClass(Resource classResource);

}
