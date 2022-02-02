package at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @param <ID> instances of a class that implements {@link Object#equals(Object)} and {@link
 *             Object#hashCode()}.
 * @author Kevin Haller
 * @version 1.2
 * @since 1.2
 */
public class PartialNormalizer<ID> extends Normalizer<ID> {

  private final List<V> noneIDValues = new LinkedList<>();

  /**
   * Registers the given {@code value} without any association to any ID.
   *
   * @param value that shall be registered without any associated ID.
   */
  public void register(BigDecimal value) {
    checkArgument(value != null, "The value must not be null.");
    this.noneIDValues.add(new V(value, 1));
  }

  /**
   * Registers the given {@code value} without any association to any ID. The given value is
   * registered {@code n} numbers of time.
   *
   * @param value that shall be registered without any associated ID.
   * @param n     number of times the given {@code value} shall be registered.
   */
  public void register(BigDecimal value, long n) {
    checkArgument(value != null, "The value must not be null.");
    this.noneIDValues.add(new V(value, n));
  }

  @Override
  protected Collection<V> gatherRegisteredValues() {
    List<V> collection = new LinkedList<>(super.gatherRegisteredValues());
    collection.addAll(noneIDValues);
    return collection;
  }
}
