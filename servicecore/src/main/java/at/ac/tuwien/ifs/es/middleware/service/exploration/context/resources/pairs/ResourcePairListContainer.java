package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.pairs;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContextContainer;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box.ValueBox;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box.ValueBoxFactory;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This is an implementation of {@link ExplorationContextContainer} for {@link ResourcePairList}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
class ResourcePairListContainer extends ExplorationContextContainer<ResourcePair> {

  ResourcePairListContainer(
      Collection<ResourcePair> resourcePairCollection,
      ValueBox originalValuesMap,
      ValueBox metadata) {
    super(resourcePairCollection, originalValuesMap, metadata);
  }

  public static ResourcePairListContainer of(ResourcePairList resourcePairList) {
    return new ResourcePairListContainer(new LinkedList<>(),
        resourcePairList.values().deepCopy(),
        resourcePairList.metadata().deepCopy());
  }

  @Override
  protected ValueBox getValuesOf(ResourcePair pair, ValueBox originalValuesMap) {
    checkArgument(pair != null, "The pair must not be null.");
    checkArgument(originalValuesMap != null, "The values map must not be null.");

    Map<String, JsonNode> valuesMap = new HashMap<>();
    originalValuesMap.get(pair.getId()).ifPresent(vb -> {
      valuesMap.put(pair.getId(), vb);
    });
    originalValuesMap.get(pair.getFirst().getId()).ifPresent(vb -> {
      valuesMap.put(pair.getFirst().getId(), vb);
    });
    originalValuesMap.get(pair.getSecond().getId()).ifPresent(vb -> {
      valuesMap.put(pair.getSecond().getId(), vb);
    });
    return ValueBoxFactory.newBox(valuesMap);
  }

}
