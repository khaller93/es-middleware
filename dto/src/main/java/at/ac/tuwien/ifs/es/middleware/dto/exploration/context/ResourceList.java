package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.ResourceJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  @JsonProperty
  private Set<Resource> list;

  /**
   * Creates a new empty list of resources.
   */
  public ResourceList() {
    this.list = new HashSet<>();
  }

  /**
   * Creates a new {@link ResourceList} from the given {@code resourceList}.
   *
   * @param resourceList that shall be wrapped.
   */
  @JsonCreator
  public ResourceList(@JsonProperty("list") List<BlankNodeOrIRI> resourceList) {
    this.list = resourceList.stream().map(Resource::new).collect(Collectors.toSet());
  }

  /**
   * Creates a new {@link ResourceList} from the given {@code selectQueryResult}. It iterates over
   * the column that corresponds to the given {@code bindingName}.
   *
   * @param selectQueryResult that shall be wrapped.
   * @param bindingName the name of the binding that represent the resource to consider.
   */
  public ResourceList(SelectQueryResult selectQueryResult, String bindingName) {
    this.list = selectQueryResult.value().column(bindingName).values().stream()
        .map(r -> new Resource((BlankNodeOrIRI) r)).collect(Collectors.toSet());
  }

  @Override
  public Iterator<Resource> iterator() {
    return this.list.iterator();
  }

  @Override
  public Collection<Resource> getResultsCollection() {
    return new ArrayList<>(list);
  }

  @Override
  public void setResults(Collection<Resource> results) {
    this.list = new HashSet<>(results);
  }

  @Override
  public void removeResult(Resource result) {
    this.list.remove(result);
    this.removeValuesData(result.getId());
  }

  @Override
  public List<BlankNodeOrIRI> asResourceList() {
    return this.list.stream().map(Resource::value).collect(Collectors.toList());
  }

  @Override
  public Set<BlankNodeOrIRI> asResourceSet() {
    return this.list.stream().map(Resource::value).collect(Collectors.toSet());
  }

  @Override
  public Iterator<BlankNodeOrIRI> getResourceIterator() {
    return this.list.stream().map(Resource::value).iterator();
  }
}
