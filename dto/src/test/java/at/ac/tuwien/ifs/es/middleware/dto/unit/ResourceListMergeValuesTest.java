package at.ac.tuwien.ifs.es.middleware.dto.unit;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;

public class ResourceListMergeValuesTest extends ExplorationContextMergeValuesTest<Resource> {

  @Override
  protected ExplorationContext<Resource> getContext() {
    return new ResourceList();
  }
}
