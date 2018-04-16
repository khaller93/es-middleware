package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import java.util.List;

/**
 * Instances of this interface represent a {@link IterableExplorationContext} for which the order
 * is natural such that operations like limit or offset can be applied. {@link ResourceList} or
 * {@link ResourcePairs} are members of this type of {@link ExplorationContext}, whereas
 * property-resource pairs in the {@link Neighbourhood} do not have this natural order. Which is why
 * later is no member.
 *
 * @param <T> the type of the result for this {@link ExplorationContext}.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface OrderedExplorationResponse<T> extends IterableExplorationContext<T> {

  /**
   * Returns a list of the results following the natural order.
   *
   * @return a {@link List} of the results following the natural order.
   */
  List<T> asList();

  /**
   * Removes the result on the given {@code index} and returns the removed result. If there is no
   * result with this index, {@code null} will be returned.
   * <p/>
   * Not only the result itself is removed, but also all its stored values.
   *
   * @return the removed result, if there is one on this index, otherwise {@code null}.
   */
  T remove(int index);

}
