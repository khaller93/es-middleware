package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.pairs;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContextContainer;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IterableResourcesContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box.ValueBox;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box.ValueBoxFactory;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This is an implementation {@link ExplorationContext} that holds a list of {@link ResourcePair}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourcePairList implements IterableResourcesContext<ResourcePair> {

  @JsonProperty(value = "pairs")
  private List<ResourcePair> pairs;

  private ValueBox values;
  private ValueBox metadata;

  public ResourcePairList(List<ResourcePair> pairs) {
    this(pairs, ValueBoxFactory.newBox(), ValueBoxFactory.newBox());
  }

  @JsonCreator
  private ResourcePairList(List<ResourcePair> pairs, ValueBox values,
      ValueBox metadata) {
    checkArgument(values != null, "The passed values box can be empty, but must not be null.");
    checkArgument(metadata != null, "The passed metadata box can be empty, but must not be null.");
    this.pairs = pairs != null ? new LinkedList<>(pairs) : Collections.emptyList();
    this.values = values;
    this.metadata = metadata;
  }

  @Override
  public ValueBox values() {
    return values;
  }

  @Override
  public ValueBox metadata() {
    return metadata;
  }

  @Override
  public Stream<ResourcePair> streamOfResults() {
    return pairs.stream();
  }

  @Override
  public Iterator<Resource> getResourceIterator() {
    return asResourceSet().iterator();
  }

  @Override
  public List<Resource> asResourceList() {
    List<Resource> resources = new LinkedList<>();
    for (ResourcePair p : pairs) {
      resources.add(p.getFirst());
      resources.add(p.getSecond());
    }
    return resources;
  }

  @Override
  public Set<Resource> asResourceSet() {
    Set<Resource> resources = new HashSet<>();
    for (ResourcePair p : pairs) {
      resources.add(p.getFirst());
      resources.add(p.getSecond());
    }
    return resources;
  }

  /**
   * This is an implementation of {@link ExplorationContextContainer} for {@link ResourcePairList}.
   */
/*  private static final class ResourcePairListContainer extends
      ExplorationContextContainer<ResourcePair> {

    private ResourcePairListContainer(
        ValueBox originalValuesMap,
        ValueBox metadata,
        Collection<ResourcePair> resultCollection) {
      super(originalValuesMap, metadata, resultCollection);
    }

    public static ResourcePairListContainer of(ResourcePairList resourcePairs) {
      return new ResourcePairListContainer(resourcePairs.values(),
          resourcePairs.metadata(), new LinkedList<>());
    }

    @Override
    protected ValueBox getValuesOf(ResourcePair result,
        ValueBox originalValuesMap) {
      ValueBox valueBox = ValueBoxFactory.newBox();
      Optional<JsonNode> pairValuesNodeOptional = originalValuesMap.get(result.getId());
      if (pairValuesNodeOptional.isPresent()) {
        valueBox.put(result.getId(), pairValuesNodeOptional.get());
      }
      Optional<JsonNode> firstNodeOptional = originalValuesMap.get(result.getFirst().getId());
      if (firstNodeOptional.isPresent()) {
        valueBox.put(result.getFirst().getId(), firstNodeOptional.get());
      }
      Optional<JsonNode> secondNodeOptional = originalValuesMap.get(result.getSecond().getId());
      if (secondNodeOptional.isPresent()) {
        valueBox.put(result.getFirst().getId(), secondNodeOptional.get());
      }
      return valueBox;
    }
  }*/

}
