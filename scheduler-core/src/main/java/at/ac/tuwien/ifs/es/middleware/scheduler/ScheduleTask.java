package at.ac.tuwien.ifs.es.middleware.scheduler;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.scheduler.behaviour.RetryBehaviour;
import java.util.Collections;
import java.util.Set;

/**
 * This class represents a task that can be scheduled in a pipeline.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ScheduleTask implements Runnable {

  private final String taskId;
  private final long timestamp;

  private final Runnable task;
  private final Set<String> neededRequirements;
  private final Set<String> providedRequirements;
  private final RetryBehaviour behaviour;

  public ScheduleTask(String taskId, long timestamp, Runnable task, Set<String> neededRequirements,
      Set<String> providedRequirements,
      RetryBehaviour behaviour) {
    checkArgument(taskId != null && !taskId.isEmpty(), "The task ID must not be null or empty.");
    checkArgument(timestamp == -1 || timestamp >= 0, "The timestamp must be positive or '-1'.");
    checkArgument(task != null, "The given task must not be null.");
    checkArgument(behaviour != null, "The behaviour for task must not be null.");
    this.taskId = taskId;
    this.timestamp = timestamp;
    this.task = task;
    this.neededRequirements =
        neededRequirements != null ? neededRequirements : Collections.emptySet();
    this.providedRequirements =
        providedRequirements != null ? providedRequirements : Collections.emptySet();
    this.behaviour = behaviour;
  }

  /**
   * Gets the id of this task.
   *
   * @return the id of this task.
   */
  public String getTaskId() {
    return taskId;
  }

  /**
   * Gets the timestamp for which the task shall be issued.
   *
   * @return the timestamp for which the task shall be issued.
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Gets a list of requirement names of which this task is dependent on.
   *
   * @return a list of requirement names of which this task is dependent on.
   */
  public Set<String> getNeededRequirements() {
    return neededRequirements;
  }

  /**
   * Gets a list of requirement names that this task is providing.
   *
   * @return a list of requirement names that this task is providing.
   */
  public Set<String> getProvidedRequirements() {
    return providedRequirements;
  }

  /**
   * gets the retry {@link RetryBehaviour} for this task.
   *
   * @return the {@link RetryBehaviour}.of this task.
   */
  public RetryBehaviour getBehaviour() {
    return behaviour;
  }

  @Override
  public void run() {
    task.run();
  }

  @Override
  public String toString() {
    return "ScheduleTask{" +
        "taskId='" + taskId + '\'' +
        ", timestamp=" + timestamp +
        ", task=" + task +
        ", neededRequirements=" + neededRequirements +
        ", providedRequirements=" + providedRequirements +
        ", behaviour=" + behaviour +
        '}';
  }
}
