package at.ac.tuwien.ifs.es.middleware.common.exploration;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.ExplorationContext;
import java.util.function.BiFunction;

/**
 * An {@link ExplorationFlowStep} represents an operation in an exploration flow.
 *
 * @param <P> the POJO class holding expected parameters.
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface ExplorationFlowStep<I extends ExplorationContext, O extends ExplorationContext,
    P extends ExplorationFlowStepPayload> extends BiFunction<I, P, O> {

  /**
   * Gets the unique id of this exploration flow step.
   *
   * @return the unique id of this exploration flow step. It must not be null or an empty string.
   */
  String getUID();

  /**
   * Gets the {@link Class} that is expected as input {@link ExplorationContext}.
   *
   * @return the {@link Class} that is expected as input {@link ExplorationContext}.
   */
  Class<I> getExplorationContextInputClass();

  /**
   * Gets the {@link Class} that is expected as output {@link ExplorationContext}.
   *
   * @return the {@link Class} that is expected as output {@link ExplorationContext}.
   */
  Class<O> getExplorationContextOutputClass();

  /**
   * Gets the {@link Class} of the {@link ExplorationFlowStepPayload} expected by this operator.
   *
   * @return the {@link Class} of the {@link ExplorationFlowStepPayload} expected by this operator.
   */
  Class<P> getPayloadClass();

  /**
   * Applies this step based on the given {@code context} and {@code parameter}. The resulting
   * {@link ExplorationContext} will be returned.
   *
   * @param context {@link ExplorationContext} for the application.
   * @param payload specifying arguments for the application.
   * @return {@link ExplorationContext} resulting from the application get this step.
   */
  @Override
  O apply(I context, P payload);

}
