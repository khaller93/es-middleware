package at.ac.tuwien.ifs.es.middleware.service.analysis;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
class AnalysisPipeline {

  private static final Logger logger = LoggerFactory.getLogger(AnalysisPipeline.class);

  private long eventId;
  private List<AnalysisServiceEntry> entries;
  private TaskExecutor taskExecutor;

  private Lock pipelineLock = new ReentrantLock();

  private AnalysisPipeline(long eventId, List<AnalysisServiceEntry> entries,
      TaskExecutor taskExecutor) {
    this.eventId = eventId;
    this.entries = new LinkedList<>(entries);
    this.taskExecutor = taskExecutor;
  }

  /**
   * Creates a new {@link AnalysisPipeline} that computes the registered pipeline.
   *
   * @param eventId {@link Long} id get the event causing a change to the knowledge graph.
   * @param entries a list get all the analysis services as {@link AnalysisServiceEntry}.
   * @param taskExecutor that shall be used to execute the single analysis tasks.
   */
  public static AnalysisPipeline of(long eventId, List<AnalysisServiceEntry> entries,
      TaskExecutor taskExecutor) {
    return new AnalysisPipeline(eventId, entries, taskExecutor);
  }

  /**
   * Registers the availability get the given {@code serviceCLasses}.
   *
   * @param serviceClasses that shall be registered to be available.
   */
  public void registerAvailabilityOf(Class<?>... serviceClasses) {
    pipelineLock.lock();
    try {
      entries.forEach(entry -> {
        for (Class<?> serviceClass : serviceClasses) {
          entry.removeRequirement(serviceClass);
        }
      });
      Map<Boolean, List<AnalysisServiceEntry>> entrySplitMap = entries.stream()
          .collect(Collectors.groupingBy(AnalysisServiceEntry::hasOpenRequirements));
      this.entries = entrySplitMap.getOrDefault(true, Collections.emptyList());
      logger.debug("Waiting services in the pipeline with id '{}' : {}.", eventId,
          entries.stream().map(AnalysisServiceEntry::getName).collect(Collectors.toList()));
      List<AnalysisServiceEntry> servicesReadyList = entrySplitMap
          .getOrDefault(false, Collections.emptyList());
      logger.debug("Ready services in the pipeline with id '{}': {}.", eventId,
          servicesReadyList.stream().map(AnalysisServiceEntry::getName)
              .collect(Collectors.toList()));
      servicesReadyList.forEach(entry -> {
        taskExecutor.execute(
            new Task(entry.getName(), entry.getAnalysisService(),
                entry.getImplementedAnalysisServiceClasses(),
                entry.isDisabled()));
      });
    } finally {
      pipelineLock.unlock();
    }
  }

  /**
   *
   */
  class Task implements Runnable {

    private String name;
    private AnalysisService analysisService;
    private Set<Class<? extends AnalysisService>> implementedServices;
    private boolean disabled;

    private Task(String name, AnalysisService analysisService,
        Set<Class<? extends AnalysisService>> implementedServices,
        boolean disabled) {
      this.name = name;
      this.analysisService = analysisService;
      this.implementedServices = implementedServices;
      this.disabled = disabled;
    }

    @Override
    public void run() {
      if (!disabled) {
        logger.info("'{}' was put into the pipeline with id '{}'.", name, eventId);
        try {
          analysisService.compute();
          logger.info("'{}' has been computed in the pipeline with id '{}'.", name, eventId);
          logger.debug("'{}' offers {}.", name, implementedServices);
          registerAvailabilityOf(implementedServices.toArray(new Class[0]));
        } catch (Exception e) {
          logger.info("'{}' has failed in the pipeline with id '{}'. {}", name, eventId,
              e.getMessage());
        }
      } else {
        logger.info("'{}' was not put into the pipeline with id '{}', because it was disabled.",
            name, eventId);
      }
    }
  }

}
