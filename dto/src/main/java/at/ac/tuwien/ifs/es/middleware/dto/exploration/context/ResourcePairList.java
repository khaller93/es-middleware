package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This is an implementation {@link ExplorationContext} that holds a list of {@link ResourcePair}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourcePairList extends AbstractExplorationContext<ResourcePair> implements IterableResourcesContext<ResourcePair> {

  @JsonProperty(value = "pairs")
  private List<ResourcePair> pairs;

  @JsonCreator
  public ResourcePairList(@JsonProperty(value = "pairs") List<ResourcePair> pairs) {
    this.pairs = pairs != null ? pairs : Collections.emptyList();
  }

  private ResourcePairList(List<ResourcePair> pairs, Map<String, ObjectNode> values,
      Map<String, JsonNode> metadata) {
    super(values, metadata);
    this.pairs = pairs != null ? pairs : Collections.emptyList();
  }

  @Override
  public Stream<ResourcePair> streamOfResults() {
    return pairs.stream();
  }

  @Override
  public Iterator<ResourcePair> iterator() {
    return pairs.stream().iterator();
  }

  @Override
  public Supplier<ExplorationContextContainer<ResourcePair>> supplier() {
    return () -> ResourcePairListContainer.of(this);
  }

  @Override
  public BiConsumer<ExplorationContextContainer<ResourcePair>, ResourcePair> accumulator() {
    return ExplorationContextContainer::addResult;
  }

  @Override
  public BinaryOperator<ExplorationContextContainer<ResourcePair>> combiner() {
    return null;
  }

  @Override
  public Function<ExplorationContextContainer<ResourcePair>, ExplorationContext<ResourcePair>> finisher() {
    return (container) -> new ResourcePairList((List<ResourcePair>) container.getResultCollection(),
        container.getValuesMap(), container.getMetadata());
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.unmodifiableSet(new HashSet<>());
  }

  @Override
  public Iterator<Resource> getResourceIterator() {
    return asResourceSet().iterator();
  }

  @Override
  public List<Resource> asResourceList() {
    List<Resource> resources = new LinkedList<>();
    for(ResourcePair p : pairs){
      resources.add(p.getFirst());
      resources.add(p.getSecond());
    }
    return resources;
  }

  @Override
  public Set<Resource> asResourceSet() {
    Set<Resource> resources = new HashSet<>();
    for(ResourcePair p : pairs){
      resources.add(p.getFirst());
      resources.add(p.getSecond());
    }
    return resources;
  }

  /**
   * This is an implementation of {@link ExplorationContextContainer} for {@link ResourcePairList}.
   */
  private static final class ResourcePairListContainer extends
      ExplorationContextContainer<ResourcePair> {

    private ResourcePairListContainer(
        Map<String, ObjectNode> originalValuesMap,
        Map<String, JsonNode> metadata,
        Collection<ResourcePair> resultCollection) {
      super(originalValuesMap, metadata, resultCollection);
    }

    public static ResourcePairListContainer of(ResourcePairList resourcePairs) {
      return new ResourcePairListContainer(resourcePairs.getAllValues(),
          resourcePairs.getMetadata(), new LinkedList<>());
    }

    @Override
    protected Map<String, ObjectNode> getValuesOf(ResourcePair result,
        Map<String, ObjectNode> originalValuesMap) {
      Map<String, ObjectNode> valuesMap = new HashMap<>();
      ObjectNode pairValuesNode = originalValuesMap.get(result.getId());
      if (pairValuesNode != null) {
        valuesMap.put(result.getId(), pairValuesNode);
      }
      ObjectNode firstNode = originalValuesMap.get(result.getFirst().getId());
      if (firstNode != null) {
        valuesMap.put(result.getFirst().getId(), firstNode);
      }
      ObjectNode secondNode = originalValuesMap.get(result.getSecond().getId());
      if (secondNode != null) {
        valuesMap.put(result.getFirst().getId(), secondNode);
      }
      return valuesMap;
    }
  }

}
