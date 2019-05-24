package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContextContainer;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box.ValueBox;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box.ValueBoxFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * This is an implementation of {@link ExplorationContextContainer} for {@link ResourceList}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
class ResourceCollectionContainer extends ExplorationContextContainer<Resource> {

  ResourceCollectionContainer(
      Collection<Resource> resultCollection,
      ValueBox originalValuesMap,
      ValueBox metadata) {
    super(resultCollection, originalValuesMap, metadata);
  }

  public static ResourceCollectionContainer of(ResourceCollection resourceCollection) {
    return new ResourceCollectionContainer(new LinkedList<>(),
        resourceCollection.values().deepCopy(),
        resourceCollection.metadata().deepCopy());
  }

  @Override
  protected ValueBox getValuesOf(Resource result, ValueBox originalValuesMap) {
    return originalValuesMap.get(result.getId()).map(jsonNode -> ValueBoxFactory
        .newBox(Collections.singletonMap(result.getId(), jsonNode)))
        .orElseGet(ValueBoxFactory::newBox);
  }
}
