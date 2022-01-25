package at.ac.tuwien.ifs.es.middleware.scheduler;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.scheduler.TaskStatus.VALUE;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * A class maintaining a task pipeline that can be filled from all parts of the application.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@EnableScheduling
@Component
public class SchedulerPipeline {

  private static final Logger logger = LoggerFactory.getLogger(SchedulerPipeline.class);

  private final DB db;
  private final HTreeMap<String, TaskStatus> taskMap;
  private final TaskExecutor threadPool;

  private final Lock openLock = new ReentrantLock();
  private final Lock runningLock = new ReentrantLock();
  private final Lock mapLock = new ReentrantLock();

  private List<ScheduleTask> openTasksList = new LinkedList<>();
  private final List<ScheduleTask> runningTasksList = new LinkedList<>();
  private final ConcurrentLinkedQueue<ScheduleTask> finishedTasksList = new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<ScheduleTask> failedTasksList = new ConcurrentLinkedQueue<>();

  private final ConcurrentMap<String, TaskChangeListener> changeListenerMap = new ConcurrentHashMap<>();

  public SchedulerPipeline(DB db, TaskExecutor threadPool) {
    this.db = db;
    this.threadPool = threadPool;
    this.taskMap = db.hashMap("scheduler.pipeline").keySerializer(Serializer.STRING)
        .<TaskStatus>valueSerializer(Serializer.JAVA).createOrOpen();
  }

  /**
   * Pushes the given {@link ScheduleTask}s to the opening tasks.
   *
   * @param scheduleTasks which shall be pushed to open tasks.
   */
  public void pushTasks(List<ScheduleTask> scheduleTasks) {
    openLock.lock();
    try {
      openTasksList.addAll(scheduleTasks);
    } finally {
      openLock.unlock();
    }
    threadPool.execute(this::checkPipeline);
  }

  /**
   * Adds the given {@link ScheduleTask} to the list of running tasks.
   *
   * @param scheduleTask which shall be switched to the running task list.
   */
  private void addRunningTask(ScheduleTask scheduleTask) {
    runningLock.lock();
    try {
      runningTasksList.add(scheduleTask);
    } finally {
      runningLock.unlock();
    }
  }

  /**
   * Removes the given {@link ScheduleTask} from the list of running tasks.
   *
   * @param scheduleTask which shall be removed from the running task list.
   */
  private void removeRunningTask(ScheduleTask scheduleTask) {
    runningLock.lock();
    try {
      runningTasksList.remove(scheduleTask);
    } finally {
      runningLock.unlock();
    }
  }

  /**
   * Retries the given {@link ScheduleTask}, if the behaviour of the task allows it.
   *
   * @param scheduleTask which shall be retried.
   */
  private void nextTryOf(ScheduleTask scheduleTask) {
    mapLock.lock();
    try {
      TaskStatus taskStatus = taskMap.get(scheduleTask.getTaskId());
      if (scheduleTask.getBehaviour().retry(taskStatus)) {
        openLock.lock();
        try {
          openTasksList.add(scheduleTask);
        } finally {
          openLock.unlock();
        }
      } else {
        failedTasksList.add(scheduleTask);
      }
    } finally {
      mapLock.unlock();
    }
  }

  /**
   *
   */
  public void registerChangeListener(String id, TaskChangeListener changeListener) {
    checkArgument(id != null && !id.isEmpty(), "The given id must not be null or empty.");
    checkArgument(changeListener != null, "The given change listener must not be null.");
    changeListenerMap.put(id, changeListener);
    threadPool.execute(() -> {
      mapLock.lock();
      try {
        if (taskMap.containsKey(id)) {
          informTaskChangeListener(id, taskMap.get(id));
        }
      } finally {
        mapLock.unlock();
      }
    });
  }

  /**
   *
   */
  public void registerChangeListener(List<String> ids, TaskChangeListener changeListener) {
    for (String id : ids) {
      registerChangeListener(id, changeListener);
    }
  }

  /**
   * @param id            for which the listener shall be registered. It must not be null.
   * @param newTaskStatus new {@link TaskStatus} passed to the change listener. It must not be
   *                      null.
   */
  private void informTaskChangeListener(String id, TaskStatus newTaskStatus) {
    if (changeListenerMap.containsKey(id)) {
      threadPool.execute(() -> changeListenerMap.get(id).onChange(id, newTaskStatus));
    }
  }

  /**
   * Persists the new status for the given scheduled task, and it increases the number of attempts
   * in the status.
   *
   * @param scheduleTask for which the new state shall be persisted.
   * @param status       the new {@link TaskStatus.VALUE} of the given {@link ScheduleTask}.
   */
  private void persistTaskStatus(ScheduleTask scheduleTask, TaskStatus.VALUE status) {
    mapLock.lock();
    try {
      TaskStatus taskStatus = taskMap.get(scheduleTask.getTaskId());
      TaskStatus newTaskStatus = null;
      if (taskStatus == null || taskStatus.getTimestamp() < scheduleTask.getTimestamp()) {
        newTaskStatus = new TaskStatus(scheduleTask.getTimestamp(), status, 1);
      } else if (taskStatus.getTimestamp() == scheduleTask.getTimestamp()) {
        newTaskStatus = new TaskStatus(scheduleTask.getTimestamp(), status,
            taskStatus.getAttempts() + 1);
      }
      if (newTaskStatus != null) {
        taskMap.put(scheduleTask.getTaskId(), newTaskStatus);
        informTaskChangeListener(scheduleTask.getTaskId(), newTaskStatus);
        if (VALUE.OK.equals(status)) {
          for (String providedRequirement : scheduleTask.getProvidedRequirements()) {
            taskMap.put(providedRequirement, newTaskStatus);
            informTaskChangeListener(providedRequirement, newTaskStatus);
          }
        }
      }
      threadPool.execute(this::checkPipeline);
      db.commit();
    } finally {
      mapLock.unlock();
    }
  }

  /**
   * Checks whether the requirements for the given {@link ScheduleTask} are fulfilled, i.e. all the
   * requirements must have been executed successfully before and not be outdated.
   *
   * @param scheduleTask for which the requirements shall be checked. It must not be null.
   * @return {@code true}, if the requirements are fulfilled, otherwise {@code false}.
   */
  private boolean isRequirementFulfilled(ScheduleTask scheduleTask) {
    for (String requirement : scheduleTask.getNeededRequirements()) {
      TaskStatus taskStatus = taskMap.get(requirement);
      if (taskStatus == null || taskStatus.getTimestamp() < scheduleTask.getTimestamp()
          || TaskStatus.VALUE.FAILED.equals(taskStatus.getStatus())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether the given {@link ScheduleTask} is outdated or not.
   *
   * @param scheduleTask for which the requirements shall be checked. It must not be null.
   * @return {@code true}, if the scheduled task is outdated, otherwise {@code false}.
   */
  private boolean isOutdated(ScheduleTask scheduleTask) {
    TaskStatus taskStatus = taskMap.get(scheduleTask.getTaskId());
    if (taskStatus != null) {
      if (VALUE.OK.equals(taskStatus.getStatus())) {
        return scheduleTask.getTimestamp() <= taskStatus.getTimestamp();
      }
    }
    return false;
  }

  @Scheduled(fixedRateString = "${esm.scheduler.slot.duration:#{10000}}")
  private void checkPipeline() {
    openLock.lock();
    try {
      mapLock.lock();
      try {
        openTasksList = openTasksList.stream().filter(t -> !isOutdated(t))
            .collect(Collectors.toList());
        List<ScheduleTask> tasksToRemove = new LinkedList<>();
        /* issue task with fulfilled requirements */
        List<Runnable> runnables = new LinkedList<>();
        for (ScheduleTask scheduleTask : openTasksList.stream()
            .filter(this::isRequirementFulfilled)
            .collect(Collectors.toList())) {
          tasksToRemove.add(scheduleTask);
          addRunningTask(scheduleTask);
          runnables.add(() -> {
            try {
              scheduleTask.run();
              finishedTasksList.add(scheduleTask);
              persistTaskStatus(scheduleTask, TaskStatus.VALUE.OK);
            } catch (Exception e) {
              logger.error("Task '{}' failed with exception {}.", scheduleTask.getTaskId(),
                  e.getMessage());
              e.printStackTrace();
              persistTaskStatus(scheduleTask, TaskStatus.VALUE.FAILED);
              nextTryOf(scheduleTask);
            } finally {
              removeRunningTask(scheduleTask);
            }
          });
        }
        /* remove issued tasks from opening list */
        openTasksList.removeAll(tasksToRemove);
        /* finally issue them */
        runnables.forEach(threadPool::execute);
      } finally {
        if (!openTasksList.isEmpty()) {
          logger.info("Open tasks: [{}]",
              openTasksList.stream()
                  .map(t -> String.format("%s(%s)", t.getTaskId(), t.getNeededRequirements()))
                  .collect(Collectors.joining(",")));
        }
        if (!runningTasksList.isEmpty()) {
          logger.info("Running tasks: [{}]",
              runningTasksList.stream().map(ScheduleTask::getTaskId)
                  .collect(Collectors.joining(",")));
        }
        if (!finishedTasksList.isEmpty()) {
          logger.info("Finished tasks: [{}]",
              finishedTasksList.stream().map(ScheduleTask::getTaskId)
                  .collect(Collectors.joining(",")));
        }
        if (!failedTasksList.isEmpty()) {
          logger.info("Failed tasks: [{}]",
              failedTasksList.stream().map(ScheduleTask::getTaskId)
                  .collect(Collectors.joining(",")));
        }
        mapLock.unlock();
      }
    } finally {
      openLock.unlock();
    }
  }

}
