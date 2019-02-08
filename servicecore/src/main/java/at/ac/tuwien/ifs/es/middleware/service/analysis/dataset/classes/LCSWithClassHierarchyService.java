package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.ClassHierarchyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ResourceClassService;
import java.util.List;
import java.util.Set;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@RegisterForAnalyticalProcessing(name = LCSWithClassHierarchyService.LCS_UID, prerequisites = {
    AllResourcesService.class, ResourceClassService.class, ClassHierarchyService.class})
public class LCSWithClassHierarchyService implements LeastCommonSubsumersService {

  public static final String LCS_UID = "esm.service.analytics.dataset.lcs.hierarchy";

  private final AllResourcesService allResourcesService;
  private final ResourceClassService resourceClassService;
  private final ClassHierarchyService classHierarchyService;
  private final DB mapDB;

  @Autowired
  public LCSWithClassHierarchyService(
      AllResourcesService allResourcesService,
      ResourceClassService resourceClassService,
      ClassHierarchyService classHierarchyService, @Qualifier("persistent-mapdb") DB mapDB) {
    this.allResourcesService = allResourcesService;
    this.resourceClassService = resourceClassService;
    this.classHierarchyService = classHierarchyService;
    this.mapDB = mapDB;
  }

  @Override
  public Set<Resource> getLeastCommonSubsumersFor(ResourcePair resourcePair) {
    return null;
  }

  @Override
  public void compute() {

  }
}
