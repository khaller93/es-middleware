package at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.IterableResourcesContext;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.ResultCollectionContext;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;

/**
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface ResourceCollection extends IterableResourcesContext<Resource>,
    ResultCollectionContext<Resource> {


}
