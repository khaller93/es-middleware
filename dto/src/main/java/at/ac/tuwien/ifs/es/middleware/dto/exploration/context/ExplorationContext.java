package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box.ValueBox;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Instances of this interface represent an intermediate or final result of an exploration flow.
 *
 * @param <T> the type of the result for this {@link ExplorationContext}, which must be
 * identifiable.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface ExplorationContext<T extends IdentifiableResult>  {

  /**
   * Gets the {@link ValueBox} maintaining the values for the results.
   *
   * @return the {@link ValueBox} maintaining the values for the results.
   */
  @JsonProperty("values")
  ValueBox values();

  /**
   * Gets the {@link ValueBox} maintaining the metadata for the results.
   *
   * @return  the {@link ValueBox} maintaining the metadata for the results.
   */
  @JsonProperty("metadata")
  ValueBox metadata();

  /**
   * Gets a {@link Stream} of the results of this {@link ExplorationContext}.
   *
   * @return a {@link Stream} of the results of this {@link ExplorationContext}.
   */
  Stream<T> streamOfResults();

}
