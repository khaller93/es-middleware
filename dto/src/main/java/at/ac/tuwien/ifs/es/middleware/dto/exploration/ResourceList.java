package at.ac.tuwien.ifs.es.middleware.dto.exploration;

import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
public class ResourceList extends ExplorationContext implements
    IterableExplorationContext<BlankNodeOrIRI>,
    IterableResourcesContext {

  @JsonProperty("list")
  @JsonSerialize(contentUsing = ResourceJsonUtil.Serializer.class)
  @JsonDeserialize(contentUsing = ResourceJsonUtil.Deserializer.class)
  private List<BlankNodeOrIRI> list;

  /**
   * Creates a new empty list of resources.
   */
  public ResourceList() {
    this.list = new LinkedList<>();
  }

  /**
   * Creates a new {@link ResourceList} from the given {@code resourceList}.
   *
   * @param resourceList that shall be wrapped.
   */
  @JsonCreator
  public ResourceList(@JsonProperty("list") List<BlankNodeOrIRI> resourceList) {
    this.list = new LinkedList<>(resourceList);
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
        .map(r -> (BlankNodeOrIRI) r).collect(Collectors.toList());
  }

  @Override
  public Iterator<BlankNodeOrIRI> iterator() {
    return this.list.iterator();
  }

  /**
   * Gets the number of resources in the list.
   *
   * @return the number of resources.
   */
  public int size() {
    return this.list.size();
  }

  @Override
  public <T extends ExplorationContext> T deepCopy() {
    return (T) new ResourceList(this.list);
  }

  @Override
  public List<BlankNodeOrIRI> asResourceList() {
    return null;
  }

  @Override
  public Set<BlankNodeOrIRI> asResourceSet() {
    return null;
  }
}
