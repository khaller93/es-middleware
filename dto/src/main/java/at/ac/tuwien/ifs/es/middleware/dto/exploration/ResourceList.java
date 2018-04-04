package at.ac.tuwien.ifs.es.middleware.dto.exploration;

import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * This is an implementation of {@link ExplorationResult} that contains a list of resources.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourceList implements IterableExplorationResult<BlankNodeOrIRI> {

  @JsonProperty("list")
  @JsonSerialize(contentUsing = ResourceJsonUtil.Serializer.class)
  @JsonDeserialize(contentUsing = ResourceJsonUtil.Deserializer.class)
  private List<BlankNodeOrIRI> list;

  /**
   * Creates a new empty list of resources.
   */
  public ResourceList() {
    this.list = Collections.emptyList();
  }

  /**
   * Creates a new {@link ResourceList} from the given {@code resourceList}.
   *
   * @param resourceList that shall be wrapped.
   */
  @JsonCreator
  public ResourceList(@JsonProperty("list") List<BlankNodeOrIRI> resourceList) {
    this.list = resourceList;
  }

  /**
   * Creates a new {@link ResourceList} from the given {@code selectQueryResult}.
   *
   * @param selectQueryResult that shall be wrapped.
   */
  public ResourceList(SelectQueryResult selectQueryResult) {

  }

  @Override
  public String getValueKey(BlankNodeOrIRI value) {
    return value.toString();
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

}
