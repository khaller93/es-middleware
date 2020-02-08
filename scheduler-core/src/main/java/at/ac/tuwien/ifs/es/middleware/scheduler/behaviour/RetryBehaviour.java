package at.ac.tuwien.ifs.es.middleware.scheduler.behaviour;

import at.ac.tuwien.ifs.es.middleware.scheduler.TaskStatus;

/**
 * Instances of this interface specify a retry behaviour.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface RetryBehaviour {

  /**
   * Checks for the task of the given {@link TaskStatus} shall be retried. Returns {@code true}, if
   * the task shall be retried, otherwise {@code false}.
   *
   * @param taskStatus for which it shall be checked.
   * @return {@code true}. if the task shall be retried, otherwise {@code false}.
   */
  boolean retry(TaskStatus taskStatus);

}
