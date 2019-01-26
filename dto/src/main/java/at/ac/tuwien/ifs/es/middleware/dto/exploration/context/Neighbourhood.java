package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.NEntry;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourceNeighbourhood;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.ResourceJsonComponent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This is an implementation of {@link ExplorationContext} that describes a neighbourhood of a
 * certain resource.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class Neighbourhood extends AbstractExplorationContext<NEntry> implements
    IterableResourcesContext {

  @JsonProperty("neighbourhood")
  @JsonSerialize(keyUsing = ResourceJsonComponent.MapSerializer.class)
  @JsonDeserialize(keyUsing = ResourceJsonComponent.MapDeserializer.class)
  private Map<Resource, ResourceNeighbourhood> resourceNeighbourhood;

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

  @JsonCreator
  public Neighbourhood(
      @JsonProperty("neighbourhood") Map<Resource, ResourceNeighbourhood> resourceNeighbourhood) {
    checkArgument(resourceNeighbourhood != null,
        "The resource neighbourhood list must not be null.");
    this.resourceNeighbourhood = new HashMap<>(resourceNeighbourhood);
  }

  private Neighbourhood(
      Map<Resource, ResourceNeighbourhood> resourceNeighbourhood,
      Map<String, ObjectNode> values,
      Map<String, JsonNode> metadata) {
    super(values, metadata);
    this.resourceNeighbourhood = resourceNeighbourhood;
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

  @Override
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
      return new Neighbourhood(nMap, container.getValuesMap(), container.getMetadata());
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.unmodifiableSet(new HashSet<>());
  }

  /**
   * This is an implementation of {@link ExplorationContextContainer} for neighbourhood entries
   * ({@link NEntry}).
   */
  private static final class NeighbourhoodContainer extends
      ExplorationContextContainer<NEntry> {

    private NeighbourhoodContainer(
        Map<String, ObjectNode> originalValuesMap,
        Map<String, JsonNode> metadata,
        Collection<NEntry> resultCollection) {
      super(originalValuesMap, metadata, resultCollection);
    }

    public static NeighbourhoodContainer of(Neighbourhood neighbourhood) {
      return new NeighbourhoodContainer(neighbourhood.getAllValues(),
          neighbourhood.getMetadata(), new LinkedList<>());
    }

    @Override
    protected Map<String, ObjectNode> getValuesOf(NEntry result,
        Map<String, ObjectNode> originalValuesMap) {
      Map<String, ObjectNode> valMap = new HashMap<>();
      ObjectNode subjectNode = originalValuesMap.get(result.getSubject().getId());
      if (subjectNode != null) {
        valMap.put(result.getSubject().getId(), subjectNode);
      }
      for (Entry<Resource, List<Resource>> e : result.getResourceNeighbourhood().getProperties()
          .entrySet()) {
        ObjectNode propertyNode = originalValuesMap.get(e.getKey().getId());
        if (propertyNode != null) {
          valMap.put(e.getKey().getId(), propertyNode);
        }
        for (Resource objectResource : e.getValue()) {
          ObjectNode objNode = originalValuesMap.get(objectResource.getId());
          valMap.put(objectResource.getId(), objNode);
        }
      }
      return valMap;
    }
  }
}
