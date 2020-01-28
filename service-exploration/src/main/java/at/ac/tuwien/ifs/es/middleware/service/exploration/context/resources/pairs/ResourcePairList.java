package at.ac.tuwien.ifs.es.middleware.service.exploration.context.resources.pairs;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ExplorationContextContainer;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.IterableResourcesContext;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.ResultCollectionContext;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.util.box.ValueBox;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.util.box.ValueBoxFactory;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * This is an implementation {@link ExplorationContext} that holds a list of {@link ResourcePair}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourcePairList implements ResultCollectionContext<ResourcePair>,
    IterableResourcesContext<ResourcePair> {

  @JsonProperty(value = "pairs")
  private List<ResourcePair> pairs;

  private ValueBox values;
  private ValueBox metadata;

  public ResourcePairList(List<ResourcePair> pairs) {
    this(pairs, ValueBoxFactory.newBox(), ValueBoxFactory.newBox());
  }

  @JsonCreator
  ResourcePairList(Collection<ResourcePair> pairs, ValueBox values,
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
  public Collector<ResourcePair, ? extends ExplorationContextContainer<ResourcePair>, ? extends ResultCollectionContext<ResourcePair>> collector() {
    return new ResourcePairCollector(this);
  }

  @Override
  public void add(ResourcePair entry) {
    checkArgument(entry != null, "The given resource pair entry must not be null.");
    pairs.add(entry);
  }

  @Override
  public void remove(ResourcePair entry) {
    checkArgument(entry != null, "The given resource pair entry must not be null.");
    pairs.remove(entry);
    values.remove(entry.getId());
    values.remove(entry.getFirst().getId());
    values.remove(entry.getSecond().getId());
  }

  @Override
  public long size() {
    return pairs.size();
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

  @Override
  public Iterator<ResourcePair> iterator() {
    return pairs.iterator();
  }

}
