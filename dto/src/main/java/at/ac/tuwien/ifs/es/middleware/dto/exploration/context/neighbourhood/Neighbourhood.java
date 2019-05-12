package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.neighbourhood;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContextContainer;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IterableResourcesContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box.ValueBox;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box.ValueBoxFactory;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.ResourceJsonComponent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This is an implementation of {@link ExplorationContext} that describes a neighbourhood of a
 * certain resource.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class Neighbourhood implements IterableResourcesContext<NEntry> {

  @JsonProperty("neighbourhood")
  @JsonSerialize(keyUsing = ResourceJsonComponent.MapSerializer.class)
  @JsonDeserialize(keyUsing = ResourceJsonComponent.MapDeserializer.class)
  private Map<Resource, ResourceNeighbourhood> resourceNeighbourhood;

  private ValueBox values;
  private ValueBox metadata;

  /**
   * Creates a {@link Neighbourhood} of the given neighbourhood map.
   *
   * @param neighbourhoodMap a map of subjects and their property maps.
   * @return a {@link Neighbourhood} that represents the given map.
   */
  public static Neighbourhood of(Map<Resource, Map<Resource, List<Resource>>> neighbourhoodMap) {
    Map<Resource, ResourceNeighbourhood> resourceNeighbourhood = new HashMap<>();
    for (Entry<Resource, Map<Resource, List<Resource>>> entry : neighbourhoodMap.entrySet()) {
      resourceNeighbourhood.put(entry.getKey(), ResourceNeighbourhood.of(entry.getValue()));
    }
    return new Neighbourhood(resourceNeighbourhood);
  }

  public Neighbourhood(
      @JsonProperty("neighbourhood") Map<Resource, ResourceNeighbourhood> resourceNeighbourhood) {
    this(resourceNeighbourhood, ValueBoxFactory.newBox(), ValueBoxFactory.newBox());
  }

  @JsonCreator
  public Neighbourhood(
      @JsonProperty("neighbourhood") Map<Resource, ResourceNeighbourhood> resourceNeighbourhood,
      @JsonProperty("values") ValueBox values,
      @JsonProperty("metadata") ValueBox metadata) {
    checkArgument(resourceNeighbourhood != null,
        "The resource neighbourhood list must not be null.");
    this.resourceNeighbourhood = new HashMap<>(resourceNeighbourhood);
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
  public Stream<NEntry> streamOfResults() {
    return resourceNeighbourhood.entrySet().stream()
        .map(ne -> new NEntry(ne.getKey(), ne.getValue()));
  }

  @Override
  public Iterator<Resource> getResourceIterator() {
    return asResourceSet().iterator();
  }

  @Override
  public List<Resource> asResourceList() {
    List<Resource> resourceSet = new LinkedList<>();
    collectResources(resourceSet);
    return resourceSet;
  }

  @Override
  public Set<Resource> asResourceSet() {
    Set<Resource> resourceSet = new HashSet<>();
    collectResources(resourceSet);
    return resourceSet;
  }

  /**
   * Collects all the {@link Resource}s occuring in this {@link Neighbourhood} and adds them to the
   * given collection.
   *
   * @param resourceCollection {@link Collection} to which all {@link Resource}s of the {@link
   * Neighbourhood} are added.
   */
  private void collectResources(Collection<Resource> resourceCollection) {
    for (Entry<Resource, ResourceNeighbourhood> resourceNeighbourhoodEntry : resourceNeighbourhood
        .entrySet()) {
      resourceCollection.add(resourceNeighbourhoodEntry.getKey());
      Map<Resource, List<Resource>> propertyMap = resourceNeighbourhoodEntry.getValue()
          .getProperties();
      for (Resource property : propertyMap.keySet()) {
        resourceCollection.add(property);
        resourceCollection.addAll(propertyMap.get(property));
      }
    }
  }

 /* @Override
  public Iterator<NEntry> iterator() {
    return resourceNeighbourhood.entrySet().stream()
        .map(ne -> new NEntry(ne.getKey(), ne.getValue())).iterator();
  }

  @Override
  public Supplier<ExplorationContextContainer<NEntry>> supplier() {
    return () -> NeighbourhoodContainer.of(this);
  }

  @Override
  public BiConsumer<ExplorationContextContainer<NEntry>, NEntry> accumulator() {
    return ExplorationContextContainer::addResult;
  }

  @Override
  public BinaryOperator<ExplorationContextContainer<NEntry>> combiner() {
    return null;
  }

  @Override
  public Function<ExplorationContextContainer<NEntry>, ExplorationContext<NEntry>> finisher() {
    return container -> {
      Map<Resource, ResourceNeighbourhood> nMap = new HashMap<>();
      for (NEntry entry : container.getResultCollection()) {
        nMap.put(entry.getSubject(), entry.getResourceNeighbourhood());
      }
      return new Neighbourhood(nMap, container.getValues(), container.getMetadata());
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.unmodifiableSet(new HashSet<>());
  }*/

  /**
   * This is an implementation of {@link ExplorationContextContainer} for neighbourhood entries
   * ({@link NEntry}).
   */
  /*private static final class NeighbourhoodContainer extends
      ExplorationContextContainer<NEntry> {

    private NeighbourhoodContainer(
        ValueBox originalValuesMap,
        ValueBox metadata,
        Collection<NEntry> resultCollection) {
      super(originalValuesMap, metadata, resultCollection);
    }

    public static NeighbourhoodContainer of(Neighbourhood neighbourhood) {
      return new NeighbourhoodContainer(neighbourhood.values(),
          neighbourhood.metadata(), new LinkedList<>());
    }

    @Override
    protected ValueBox getValuesOf(NEntry result,
        ValueBox originalValuesMap) {
      ValueBox valueBox = ValueBoxFactory.newBox();
      Optional<JsonNode> subjectNodeOptional = originalValuesMap.get(result.getSubject().getId());
      if (subjectNodeOptional.isPresent()) {
        valueBox.put(result.getSubject().getId(), subjectNodeOptional.get());
      }
      for (Entry<Resource, List<Resource>> e : result.getResourceNeighbourhood().getProperties()
          .entrySet()) {
        Optional<JsonNode> propertyNodeOptional = originalValuesMap.get(e.getKey().getId());
        if (propertyNodeOptional.isPresent()) {
          valueBox.put(e.getKey().getId(), propertyNodeOptional.get());
        }
        for (Resource objectResource : e.getValue()) {
          Optional<JsonNode> objNodeOpt = originalValuesMap.get(objectResource.getId());
          if(objNodeOpt.isPresent()) {
            valueBox.put(objectResource.getId(), objNodeOpt.get());
          }
        }
      }
      return valueBox;
    }
  }*/
}
