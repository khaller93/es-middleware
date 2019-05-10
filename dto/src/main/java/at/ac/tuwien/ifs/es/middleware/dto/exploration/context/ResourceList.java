package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
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
public class ResourceList extends AbstractExplorationContext<Resource> implements
    ResourceCollection {

  @JsonProperty("list")
  private List<Resource> list;

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
  public ResourceList(@JsonProperty("list") Collection<Resource> resourceList) {
    this.list = new LinkedList<>(resourceList);
  }

  private ResourceList(java.util.List<Resource> list, Map<String, ObjectNode> values,
      Map<String, JsonNode> metadata) {
    super(values, metadata);
    this.list = list;
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

  /**
   * Gets the size of the results list.
   *
   * @return the size of the results list.
   */
  @JsonProperty(value = "size")
  public int getSize() {
    return list.size();
  }

  @Override
  @Nonnull
  public Iterator<Resource> iterator() {
    return getResourceIterator();
  }

  @Override
  public Stream<Resource> streamOfResults() {
    return this.list.stream();
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

  @Override
  public Supplier<ExplorationContextContainer<Resource>> supplier() {
    return () -> ResultListContainer.of(this);
  }

  @Override
  public BiConsumer<ExplorationContextContainer<Resource>, Resource> accumulator() {
    return ExplorationContextContainer::addResult;
  }

  @Override
  public BinaryOperator<ExplorationContextContainer<Resource>> combiner() {
    return null;
  }

  @Override
  public Function<ExplorationContextContainer<Resource>, ExplorationContext<Resource>> finisher() {
    return container -> new ResourceList((List<Resource>) container.getResultCollection(),
        container.getValuesMap(), container.getMetadata());
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.unmodifiableSet(new HashSet<>());
  }

  /**
   * This is an implementation of {@link ExplorationContextContainer} for {@link ResourceList}.
   */
  private static final class ResultListContainer extends ExplorationContextContainer<Resource> {

    private ResultListContainer(
        Map<String, ObjectNode> originalValuesMap,
        Map<String, JsonNode> metadata,
        Collection<Resource> resultCollection) {
      super(originalValuesMap, metadata, resultCollection);
    }

    public static ResultListContainer of(ResourceList resultList) {
      return new ResultListContainer(resultList.getAllValues(),
          resultList.getMetadata(), new LinkedList<>());
    }

    @Override
    protected Map<String, ObjectNode> getValuesOf(Resource result,
        Map<String, ObjectNode> originalValuesMap) {
      ObjectNode valueNode = originalValuesMap.get(result.getId());
      if (valueNode != null) {
        return Collections.singletonMap(result.getId(), valueNode);
      } else {
        return Collections.emptyMap();
      }
    }
  }

}
