package at.ac.tuwien.ifs.es.middleware.service.analysis.normalizer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.NormalizationStrategy;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils.Normalizer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This class tests {@link Normalizer}.
 *
 * @author Kevin Haller
 * @version 1.2
 * @since 1.2
 */
public class SimpleNormalizerTests {

  private Normalizer<String> normalizer;

  protected Normalizer<String> getNormalizer() {
    return new Normalizer<>();
  }

  @BeforeEach
  public void setUp() {
    normalizer = getNormalizer();
  }

  public static Stream<Arguments> normalizationTestCases() {
    return Stream.of(
        /* a) all values are the same, namely 10 */
        Arguments.of(ImmutableMap.builder().put("a", BigDecimal.TEN).put("b", BigDecimal.TEN)
                .put("c", BigDecimal.TEN).build(),
            ImmutableMap.builder().put("a", BigDecimal.ONE).put("b", BigDecimal.ONE)
                .put("c", BigDecimal.ONE).build(),
            ImmutableMap.builder().put("a", BigDecimal.ZERO).put("b", BigDecimal.ZERO)
                .put("c", BigDecimal.ZERO).build()),
        /* b) all values are the same, namely 0 */
        Arguments.of(ImmutableMap.builder().put("a", BigDecimal.ZERO).put("b", BigDecimal.ZERO)
                .put("c", BigDecimal.ZERO).build(),
            ImmutableMap.builder().put("a", BigDecimal.ZERO).put("b", BigDecimal.ZERO)
                .put("c", BigDecimal.ZERO).build(),
            ImmutableMap.builder().put("a", BigDecimal.ZERO).put("b", BigDecimal.ZERO)
                .put("c", BigDecimal.ZERO).build()),
        /* c) all values are between 0 and 1 */
        Arguments.of(ImmutableMap.builder().put("a", BigDecimal.ZERO).put("b", BigDecimal.ONE)
                .put("c", BigDecimal.ZERO).put("d", BigDecimal.ONE).build(),
            ImmutableMap.builder().put("a", BigDecimal.ZERO).put("b", BigDecimal.ONE)
                .put("c", BigDecimal.ZERO).put("d", BigDecimal.ONE).build(),
            ImmutableMap.builder().put("a", BigDecimal.valueOf(-1))
                .put("b", BigDecimal.valueOf(1)).put("c", BigDecimal.valueOf(-1))
                .put("d", BigDecimal.valueOf(1)).build())
    );
  }

  @ParameterizedTest
  @MethodSource("normalizationTestCases")
  public void testNormalizer_mustReturnCorrectValues(Map<String, BigDecimal> values,
      Map<String, BigDecimal> expectedMinMax, Map<String, BigDecimal> expectedZScore) {
    for (Entry<String, BigDecimal> entry : values.entrySet()) {
      normalizer.register(entry.getKey(), entry.getValue());
    }
    Map<String, DecimalNormalizedAnalysisValue> normalizedMap = normalizer.normalize();
    assertThat("Maps must be of the same size", normalizedMap.entrySet(),
        hasSize(expectedMinMax.entrySet().size()));
    assertThat("Maps must be of the same size", normalizedMap.entrySet(),
        hasSize(expectedZScore.entrySet().size()));
    /* check for correct min, max values */
    assertTrue(normalizedMap.values().stream()
        .map(d -> d.getValueOfStrategy(NormalizationStrategy.MinMax).isPresent())
        .reduce((a, b) -> a && b).orElse(false), "Min,Max for all values must be present.");
    Map<String, BigDecimal> minMaxMap = new HashMap<>();
    normalizedMap.forEach(
        (key, value) -> value.getValueOfStrategy(NormalizationStrategy.MinMax).ifPresent(val -> {
          minMaxMap.put(key, val);
        }));
    MapDifference<String, BigDecimal> minMaxDifference = Maps.difference(minMaxMap, expectedMinMax);
    assertThat("Entries must not differ.", minMaxDifference.entriesDiffering().values(),
        is(empty()));
    /* check for z-score values */
    assertTrue(normalizedMap.values().stream()
        .map(d -> d.getValueOfStrategy(NormalizationStrategy.ZScore).isPresent())
        .reduce((a, b) -> a && b).orElse(false), "ZScore for all values must be present.");
    Map<String, BigDecimal> zScoreMap = new HashMap<>();
    normalizedMap.forEach(
        (key, value) -> value.getValueOfStrategy(NormalizationStrategy.ZScore).ifPresent(val -> {
          zScoreMap.put(key, val);
        }));
    MapDifference<String, BigDecimal> zScoreDifference = Maps.difference(zScoreMap, expectedZScore);
    assertThat("Entries must not differ.", zScoreDifference.entriesDiffering().values(),
        is(empty()));
  }

  @Test
  public void normalizeWithNoRegistration_mustReturnEmptyMap() {
    Map<String, DecimalNormalizedAnalysisValue> normalizedMap = normalizer.normalize();
    assertNotNull(normalizedMap);
    assertThat(normalizedMap.values(), is(empty()));
  }

  @Test
  public void registerWithNullID_mustThrowIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> normalizer.register(null, BigDecimal.ZERO));
  }

  @Test
  public void registerWithNullBigDecimalValue_mustThrowIllegalArgumentException() {
    BigDecimal value = null;
    assertThrows(IllegalArgumentException.class, () -> normalizer.register("a", value));
  }

}
