package at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.ExplorationContextContainer;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.ResultCollectionContext;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.box.ValueBox;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.box.ValueBoxFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * This is an implementation of {@link ExplorationContext} that contains a list of resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourceList implements ResourceCollection {

  @JsonProperty("list")
  private List<Resource> list;

  private ValueBox values;
  private ValueBox metadata;

  /**
   * Creates a new empty list of resources.
   */
  public ResourceList() {
    this(Collections.emptyList());
  }

  /**
   * Creates a new {@link ResourceList} from the given {@code resourceList}.
   *
   * @param resourceList that shall be wrapped.
   */
  public ResourceList(Collection<Resource> resourceList) {
    this(resourceList, ValueBoxFactory.newBox(), ValueBoxFactory.newBox());
  }

  @JsonCreator
  private ResourceList(@JsonProperty(value = "list", required = true) Collection<Resource> list,
      @JsonProperty("values") ValueBox values,
      @JsonProperty("metadata") ValueBox metadata) {
    checkArgument(values != null, "The passed values box can be empty, but must not be null.");
    checkArgument(metadata != null, "The passed metadata box can be empty, but must not be null.");
    this.list = new LinkedList<>(list);
    this.values = values;
    this.metadata = metadata;
  }

  public static ResourceList of(List<BlankNodeOrIRI> resourceList) {
    return new ResourceList(resourceList.stream().map(Resource::new).collect(Collectors.toList()));
  }

  /**
   * Creates a new {@link ResourceList} from the given {@code selectQueryResult}. It iterates over
   * the column that corresponds to the given {@code bindingName}.
   *
   * @param selectQueryResult that shall be wrapped.
   * @param bindingName the name of the binding that represent the resource to consider.
   */
  /*public static ResourceList of(SelectQueryResult selectQueryResult, String bindingName) {
    List<Resource> list = new LinkedList<>();
    for (Map<String, RDFTerm> row : selectQueryResult.value()) {
      if (row.containsKey(bindingName)) {
        list.add(new Resource((BlankNodeOrIRI) row.get(bindingName)));
      }
    }
    return new ResourceList(list);
  }*/

  @Override
  @Nonnull
  public Iterator<Resource> iterator() {
    return getResourceIterator();
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
  public Stream<Resource> streamOfResults() {
    return this.list.stream();
  }

  @Override
  public Collector<Resource, ? extends ExplorationContextContainer<Resource>, ? extends ResultCollectionContext<Resource>> collector() {
    return new RLCollector(this);
  }

  @Override
  public void add(Resource entry) {
    checkArgument(entry != null, "The given entry must not be null.");
    this.list.add(entry);
  }

  /**
   * Adds the given {@link Resource} {@code entry} at the given index in this resource list.
   *
   * @param entry {@link Resource} that shall be added.
   * @param index a positive number or 0, at which position the entry shall be added.
   */
  public void add(int index, Resource entry) {
    checkArgument(index >= 0, "The index must be a positive number or 0.");
    checkArgument(entry != null, "The given entry must not be null.");
    this.list.add(index, entry);
  }

  @Override
  public void remove(Resource entry) {
    checkArgument(entry != null, "The given entry must not be null.");
    this.list.remove(entry);
    this.values.remove(entry.getId());
  }

  @JsonProperty(value = "size")
  @Override
  public long size() {
    return list.size();
  }

  @Override
  public List<Resource> asResourceList() {
    return new LinkedList<>(this.list);
  }

  @Override
  public Set<Resource> asResourceSet() {
    return new HashSet<>(this.list);
  }

  @Override
  public Iterator<Resource> getResourceIterator() {
    return this.list.stream().iterator();
  }

  private static class RLCollector extends ResourceCollector {

    RLCollector(ResourceCollection resourceCollection) {
      super(resourceCollection);
    }

    @Override
    public Function<ResourceCollectionContainer, ResourceCollection> finisher() {
      return container -> new ResourceList((List<Resource>) container.getResultCollection(),
          container.getValues(), container.getMetadata());
    }
  }

}
