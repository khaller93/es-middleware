package at.ac.tuwien.ifs.es.middleware.scheduler;

@FunctionalInterface
public interface TaskChangeListener {

  /**
   * This method shall be called, if a task status changed.
   *
   * @param id the id for which the status change has been recognized.
   * @param status new {@link TaskStatus}.
   */
  void onChange(String id, TaskStatus status);

}
