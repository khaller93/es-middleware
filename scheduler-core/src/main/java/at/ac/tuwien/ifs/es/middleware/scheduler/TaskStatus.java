package at.ac.tuwien.ifs.es.middleware.scheduler;

import java.io.Serializable;

/**
 * This class represents a status of a specific task.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class TaskStatus implements Serializable {

  public enum VALUE {OK, FAILED}

  private long timestamp;
  private VALUE status;
  private long attempts;

  public TaskStatus(long timestamp, VALUE status, long attempts) {
    this.timestamp = timestamp;
    this.status = status;
    this.attempts = attempts;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public VALUE getStatus() {
    return status;
  }

  public long getAttempts() {
    return attempts;
  }

  @Override
  public String toString() {
    return "TaskStatus{" +
        "timestamp=" + timestamp +
        ", status=" + status +
        ", attempts=" + attempts +
        '}';
  }
}
