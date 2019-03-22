package at.ac.tuwien.ifs.es.middleware.service.exploration;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * An {@link ExplorationFlowStep} represents an operation in an {@link ExplorationFlow}.
 *
 * @param <T> the POJO class holding expected parameters.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface ExplorationFlowStep<T extends Serializable> extends
    BiFunction<ExplorationContext, T, ExplorationContext> {

  /**
   * Gets the unique id of this exploration flow step.
   *
   * @return the unique id of this exploration flow step. It must not be null or an empty string.
   */
  String getUID();

  /**
   * This method returns a POJO that matches the expected parameters for this exploration step.
   *
   * @return a POJO for matching the parameters expected for this {@link ExplorationFlowStep}.
   */
  Class<T> getParameterClass();

  /**
   * Applies this step based on the given {@code context} and {@code parameter}. The resulting
   * {@link ExplorationContext} will be returned.
   *
   * @param context {@link ExplorationContext} for the application.
   * @param payload specifying arguments for the application.
   * @return {@link ExplorationContext} resulting from the application get this step.
   */
  @Override
  ExplorationContext apply(ExplorationContext context, T payload);
}
