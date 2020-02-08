package at.ac.tuwien.ifs.es.middleware.scheduler.behaviour;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.scheduler.TaskStatus;

/**
 * This class is a specialisation of {@link RetryBehaviour} that specifies to retry a task a given
 * number of times.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class NRetryBehaviour implements RetryBehaviour {

  private int maxRetries;

  /**
   * Creates a new {@link RetryBehaviour} that will allow the given number of maximal attempts
   * {@code maxRetries}.
   *
   * @param maxRetries the maximum number of tries.
   */
  private NRetryBehaviour(int maxRetries) {
    checkArgument(maxRetries > 0, "The number of maximal tries must be positive.");
    this.maxRetries = maxRetries;
  }

  /**
   * Gets a new {@link RetryBehaviour} that will allow the given number of maximal attempts {@code
   * maxRetries}.
   *
   * @param maxRetries the maximum number of tries.
   * @return a new {@link RetryBehaviour} that will allow the given number of maximal attempts
   * {@code maxRetries}.
   */
  public static NRetryBehaviour of(int maxRetries) {
    return new NRetryBehaviour(maxRetries);
  }

  @Override
  public boolean retry(TaskStatus taskStatus) {
    return taskStatus.getAttempts() <= maxRetries;
  }
}
