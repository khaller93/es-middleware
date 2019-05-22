package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.neighbourhood;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IterableObjectsContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IterablePredicatesContext;
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
public class Neighbourhood implements IterableResourcesContext<NEntry>,
    IterablePredicatesContext<NEntry>,
    IterableObjectsContext<NEntry> {

  @JsonProperty("neighbourhood")
  @JsonSerialize(keyUsing = ResourceJsonComponent.MapSerializer.class)
  @JsonDeserialize(keyUsing = ResourceJsonComponent.MapDeserializer.class)
  private Map<Resource, RHood> resourceNeighbourhood;

  private ValueBox values;
  private ValueBox metadata;

  /**
   * Creates a {@link Neighbourhood} of the given neighbourhood map.
   *
   * @param neighbourhoodMap a map of subjects and their property maps.
   * @return a {@link Neighbourhood} that represents the given map.
   */
  public static Neighbourhood of(Map<Resource, Map<Resource, List<RDFTerm>>> neighbourhoodMap) {
    Map<Resource, RHood> resourceNeighbourhood = new HashMap<>();
    for (Entry<Resource, Map<Resource, List<RDFTerm>>> entry : neighbourhoodMap.entrySet()) {
      resourceNeighbourhood.put(entry.getKey(), RHood.of(entry.getValue()));
    }
    return new Neighbourhood(resourceNeighbourhood);
  }

  public Neighbourhood(
      @JsonProperty("neighbourhood") Map<Resource, RHood> resourceNeighbourhood) {
    this(resourceNeighbourhood, ValueBoxFactory.newBox(), ValueBoxFactory.newBox());
  }

  @JsonCreator
  public Neighbourhood(
      @JsonProperty("neighbourhood") Map<Resource, RHood> resourceNeighbourhood,
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
    for (Entry<Resource, RHood> resourceNeighbourhoodEntry : resourceNeighbourhood
        .entrySet()) {
      resourceCollection.add(resourceNeighbourhoodEntry.getKey());
      Map<Resource, List<RDFTerm>> propertyMap = resourceNeighbourhoodEntry.getValue()
          .getProperties();
      for (Resource property : propertyMap.keySet()) {
        resourceCollection.add(property);
        propertyMap.get(property).stream().filter(term -> term instanceof Resource)
            .map(term -> (Resource) term).forEach(resourceCollection::add);
      }
    }
  }

  @Override
  public Iterator<Resource> getPredicateIterator() {
    return resourceNeighbourhood.keySet().iterator();
  }

  @Override
  public List<Resource> asPredicateList() {
    return new LinkedList<>(resourceNeighbourhood.keySet());
  }

  @Override
  public Set<Resource> asPredicateSet() {
    return resourceNeighbourhood.keySet();
  }

  private List<RDFTerm> traverseObjects() {
    List<RDFTerm> objects = new LinkedList<>();
    for (RHood neighbourhood : resourceNeighbourhood.values()) {
       for(List<RDFTerm> neighObjects : neighbourhood.getProperties().values()){
         objects.addAll(neighObjects);
       }
    }
    return objects;
  }

  @Override
  public Iterator<RDFTerm> getObjectIterator() {
    return traverseObjects().iterator();
  }

  @Override
  public List<RDFTerm> asObjectList() {
    return traverseObjects();
  }

  @Override
  public Set<RDFTerm> asObjectSet() {
    return new HashSet<>(traverseObjects());
  }

}
