package at.ac.tuwien.ifs.es.middleware.service.analysis;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.KGUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.scheduler.ScheduleTask;
import at.ac.tuwien.ifs.es.middleware.scheduler.SchedulerPipeline;
import at.ac.tuwien.ifs.es.middleware.scheduler.behaviour.NRetryBehaviour;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This class handles the scheduling of DAO and analysis tasks.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Component
public class AnalysisScheduler {

  private final SchedulerPipeline schedulerPipeline;
  private final AnalysisServiceRegistry registry;

  @Value("${esm.analysis.computeFreshlyOnStart:#{false}}")
  private boolean computeFreshlyOnStart;

  @Autowired
  public AnalysisScheduler(AnalysisServiceRegistry registry,
      SchedulerPipeline schedulerPipeline) {
    this.registry = registry;
    this.schedulerPipeline = schedulerPipeline;
  }

  @EventListener
  public void onApplicationEvent(ApplicationReadyEvent event) {
    registry.scanAndRegisterAnalysisServices(event.getApplicationContext());
    pushTasks(computeFreshlyOnStart ? Instant.now().toEpochMilli() : -1,
        registry.getRegisteredAnalysisServices());
  }

  private void pushTasks(long analysisTimestamp, List<AnalysisServiceEntry> analysisServices) {
    schedulerPipeline.pushTasks(analysisServices.stream()
        .filter(as -> !as.isDisabled())
        .map(as -> new ScheduleTask(as.getName(), analysisTimestamp,
            as.getAnalysisService()::compute,
            as.getRequirements().stream().map(Class::getName).collect(Collectors.toSet()),
            as.getImplementedAnalysisServiceClasses().stream().map(Class::getName)
                .collect(Collectors.toSet()),
            NRetryBehaviour.of(3)))
        .collect(Collectors.toList()));
  }

  @EventListener
  public void onKnowledgeGraphUpdated(KGUpdatedEvent updatedEvent) {
    pushTasks(updatedEvent.getTimestamp(), registry.getRegisteredAnalysisServices());
  }

}
