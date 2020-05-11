package at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization;

import at.ac.tuwien.ifs.es.middleware.service.analysis.value.AnalysisNumberValue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Instances of this interface represent a value that has been normalized with different
 * strategies.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface NormalizedAnalysisValue<N extends Number> extends AnalysisNumberValue<N> {

  /**
   * Lists all the strategies ({@link NormalizationStrategy}) that have been performed on this
   * value.
   *
   * @return all the strategies ({@link NormalizationStrategy}) that have been performed on this
   * value.
   */
  List<NormalizationStrategy> strategies();

  /**
   * Gets the value of the given {@code strategy} ({@link NormalizationStrategy}). {@link
   * Optional#empty()} will be returned, if this strategy has not been performed.
   *
   * @param strategy {@link NormalizationStrategy} for which the value shall be returned.
   * @return value of the given {@code strategy}, or {@link Optional#empty()}, if this strategy has
   * not been performed.
   * @throws IllegalArgumentException if the given strategy is null.
   */
  Optional<BigDecimal> getValueOfStrategy(NormalizationStrategy strategy);

}
