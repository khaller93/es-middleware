package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;

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
  private List<Resource> List;

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
  @JsonCreator
  public ResourceList(@JsonProperty("list") List<Resource> resourceList) {
    this.List = new LinkedList<>(resourceList);
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
  public static ResourceList of(SelectQueryResult selectQueryResult, String bindingName) {
    List<Resource> list = new LinkedList<>();
    for (Map<String, RDFTerm> row : selectQueryResult.value()) {
      if (row.containsKey(bindingName)) {
        list.add(new Resource((BlankNodeOrIRI) row.get(bindingName)));
      }
    }
    return new ResourceList(list);
  }

  @Override
  public Iterator<Resource> iterator() {
    return this.List.iterator();
  }

  @Override
  public Collection<Resource> getResultsCollection() {
    return new ArrayList<>(List);
  }

  @Override
  public void setResults(Collection<Resource> results) {
    this.List = new LinkedList<>(results);
  }

  @Override
  public void removeResult(Resource result) {
    this.List.remove(result);
    this.removeValuesData(result.getId());
  }

  @Override
  public List<Resource> asResourceList() {
    return new LinkedList<>(this.List);
  }

  @Override
  public Set<Resource> asResourceSet() {
    return new HashSet<>(this.List);
  }

  @Override
  public Iterator<Resource> getResourceIterator() {
    return this.List.stream().iterator();
  }
}
