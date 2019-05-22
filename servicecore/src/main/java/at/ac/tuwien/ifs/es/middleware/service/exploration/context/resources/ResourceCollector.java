package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContextContainer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * This class implements a {@link Collector} for {@link ResourceCollection}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
abstract class ResourceCollector implements
    Collector<Resource, ResourceCollectionContainer, ResourceCollection> {

  private ResourceCollection resourceCollection;

  public ResourceCollector(ResourceCollection resourceCollection) {
    checkArgument(resourceCollection != null, "The given resource collection must not be null.");
    this.resourceCollection = resourceCollection;
  }

  @Override
  public Supplier<ResourceCollectionContainer> supplier() {
    return () -> ResourceCollectionContainer.of(resourceCollection);
  }

  @Override
  public BiConsumer<ResourceCollectionContainer, Resource> accumulator() {
    return ExplorationContextContainer::addResult;
  }

  @Override
  public BinaryOperator<ResourceCollectionContainer> combiner() {
    return null;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.unmodifiableSet(new HashSet<>());
  }

}
