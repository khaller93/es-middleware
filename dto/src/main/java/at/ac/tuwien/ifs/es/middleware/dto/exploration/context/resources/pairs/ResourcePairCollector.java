package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.pairs;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContextContainer;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * This class implements a {@link Collector} for {@link ResourcePairList}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
class ResourcePairCollector implements
    Collector<ResourcePair, ResourcePairListContainer, ResourcePairList> {

  private ResourcePairList resourcePairList;

  public ResourcePairCollector(ResourcePairList resourcePairList) {
    checkArgument(resourcePairList != null, "The given resource pair list must not be null.");
    this.resourcePairList = resourcePairList;
  }

  @Override
  public Supplier<ResourcePairListContainer> supplier() {
    return () -> ResourcePairListContainer.of(resourcePairList);
  }

  @Override
  public BiConsumer<ResourcePairListContainer, ResourcePair> accumulator() {
    return ExplorationContextContainer::addResult;
  }

  @Override
  public BinaryOperator<ResourcePairListContainer> combiner() {
    return null;
  }

  @Override
  public Function<ResourcePairListContainer, ResourcePairList> finisher() {
    return (container) -> new ResourcePairList(container.getResultCollection(),
        container.getValues(),
        container.getMetadata());
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.unmodifiableSet(new HashSet<>());
  }

}
