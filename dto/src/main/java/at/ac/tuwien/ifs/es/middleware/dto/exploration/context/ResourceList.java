package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * This is an implementation of {@link ExplorationContext} that contains a list of resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourceList extends AbstractExplorationContext<Resource> implements
    IterableResourcesContext {

  @JsonProperty("list")
  private Set<Resource> set;

  /**
   * Creates a new empty list of resources.
   */
  public ResourceList() {
    this.set = new HashSet<>();
  }

  /**
   * Creates a new {@link ResourceList} from the given {@code resourceList}.
   *
   * @param resourceList that shall be wrapped.
   */
  @JsonCreator
  public ResourceList(@JsonProperty("list") List<Resource> resourceList) {
    this.set = new HashSet<>(resourceList);
  }

  private ResourceList(Set<Resource> resourceSet) {
    this.set = resourceSet;
  }

  public static ResourceList of(List<BlankNodeOrIRI> resourceList) {
    return new ResourceList(resourceList.stream().map(Resource::new).collect(Collectors.toSet()));
  }

  /**
   * Creates a new {@link ResourceList} from the given {@code selectQueryResult}. It iterates over
   * the column that corresponds to the given {@code bindingName}.
   *
   * @param selectQueryResult that shall be wrapped.
   * @param bindingName the name of the binding that represent the resource to consider.
   */
  public static ResourceList of(SelectQueryResult selectQueryResult, String bindingName) {
    return new ResourceList(selectQueryResult.value().column(bindingName).values().stream()
        .map(r -> new Resource((BlankNodeOrIRI) r)).collect(Collectors.toSet()));
  }

  @Override
  public Iterator<Resource> iterator() {
    return this.set.iterator();
  }

  @Override
  public Collection<Resource> getResultsCollection() {
    return new ArrayList<>(set);
  }

  @Override
  public void setResults(Collection<Resource> results) {
    this.set = new HashSet<>(results);
  }

  @Override
  public void removeResult(Resource result) {
    this.set.remove(result);
    this.removeValuesData(result.getId());
  }

  @Override
  public List<Resource> asResourceList() {
    return new LinkedList<>(this.set);
  }

  @Override
  public Set<Resource> asResourceSet() {
    return new HashSet<>(this.set);
  }

  @Override
  public Iterator<Resource> getResourceIterator() {
    return this.set.stream().iterator();
  }
}
