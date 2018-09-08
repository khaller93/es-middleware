package at.ac.tuwien.ifs.es.middleware.service.analysis;

import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor.Entry;
import java.time.Instant;
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

  private Instant daoTimestamp;
  private List<Entry> entries;
  private TaskExecutor taskExecutor;

  private Lock pipelineLock = new ReentrantLock();

  private AnalysisPipeline(Instant daoTimestamp,
      List<Entry> entries, TaskExecutor taskExecutor) {
    this.daoTimestamp = daoTimestamp;
    this.entries = new LinkedList<>(entries);
    this.taskExecutor = taskExecutor;
  }

  /**
   * Creates a new {@link AnalysisPipeline} that computes the registered pipeline.
   *
   * @param daoTimestamp {@link Instant} of the change that initiated the update.
   * @param entries a list of all the analysis services as {@link AnalysisPipelineProcessor.Entry}.
   * @param taskExecutor that shall be used to execute the single analysis tasks.
   */
  public static AnalysisPipeline of(Instant daoTimestamp, List<Entry> entries,
      TaskExecutor taskExecutor) {
    return new AnalysisPipeline(daoTimestamp, entries, taskExecutor);
  }

  /**
   * Registers the availability of the given {@code serviceCLasses}.
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
      Map<Boolean, List<Entry>> entrySplitMap = entries.stream()
          .collect(Collectors.groupingBy(Entry::hasOpenRequirements));
      this.entries = entrySplitMap.getOrDefault(true, Collections.emptyList());
      logger.debug("Remaining services with unfulfilled requirements in the pipeline {}.", entries);
      List<Entry> servicesReadyList = entrySplitMap.getOrDefault(false, Collections.emptyList());
      logger.debug("Ready services in the pipeline {}.", servicesReadyList);
      servicesReadyList.forEach(entry -> {
        taskExecutor.execute(
            new Task(entry.getAnalysisService(), entry.getImplementedAnalysisServiceClasses()));
      });
    } finally {
      pipelineLock.unlock();
    }
  }

  class Task implements Runnable {

    private AnalysisService analysisService;
    private Set<Class<? extends AnalysisService>> implementedServices;

    public Task(AnalysisService analysisService,
        Set<Class<? extends AnalysisService>> implementedServices) {
      this.analysisService = analysisService;
      this.implementedServices = implementedServices;
    }

    @Override
    public void run() {
      logger.info("{} was put into the pipeline '{}'.", analysisService, daoTimestamp);
      analysisService.compute();
      logger.info("{} has been computed in the pipeline '{}'.", analysisService, daoTimestamp);
      logger.debug("{} offers {}.", analysisService, implementedServices);
      registerAvailabilityOf(implementedServices.toArray(new Class[0]));
    }
  }

}
