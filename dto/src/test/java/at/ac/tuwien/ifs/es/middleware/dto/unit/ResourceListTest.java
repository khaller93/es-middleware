package at.ac.tuwien.ifs.es.middleware.dto.unit;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;


/**
 * This class should unit test {@link at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourceListTest extends AbstractExplorationContextTest<Resource>{

  @Override
  protected ExplorationContext<Resource> getContext() {
    return new ResourceList();
  }
}
