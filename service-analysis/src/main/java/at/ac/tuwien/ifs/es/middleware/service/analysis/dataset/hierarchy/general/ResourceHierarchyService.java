package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.general;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.List;
import java.util.Set;

public interface ResourceHierarchyService extends AnalysisService {

  /**
   *
   * @param includeClasses
   * @param excludeClasses
   * @param topDownProperties
   * @param bottomUpProperties
   * @return
   */
  List<ResourceNode> getHierarchy(Set<Resource> includeClasses, Set<Resource> excludeClasses,
      Set<Resource> topDownProperties, Set<Resource> bottomUpProperties);

}
