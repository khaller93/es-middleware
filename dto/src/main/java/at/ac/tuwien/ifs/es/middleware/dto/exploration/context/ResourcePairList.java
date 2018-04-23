package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * //TODO: Implement.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourcePairList extends AbstractExplorationContext<ResourcePair> {

  @Override
  public Stream<ResourcePair> streamOfResults() {
    return null;
  }

  @Override
  public Iterator<ResourcePair> iterator() {
    return null;
  }

  @Override
  public Supplier<ExplorationContextContainer<ResourcePair>> supplier() {
    return null;
  }

  @Override
  public BiConsumer<ExplorationContextContainer<ResourcePair>, ResourcePair> accumulator() {
    return null;
  }

  @Override
  public BinaryOperator<ExplorationContextContainer<ResourcePair>> combiner() {
    return null;
  }

  @Override
  public Function<ExplorationContextContainer<ResourcePair>, ExplorationContext<ResourcePair>> finisher() {
    return null;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return null;
  }
}
