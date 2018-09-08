package at.ac.tuwien.ifs.es.middleware.service.analysis;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.fts.FullTextSearchDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.fts.FullTextSearchService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Component
public class AnalysisPipelineProcessorImpl implements AnalysisPipelineProcessor {

  private static final Logger logger = LoggerFactory.getLogger(AnalysisPipelineProcessorImpl.class);

  private ConcurrentLinkedQueue<Entry> analysisServices = new ConcurrentLinkedQueue<>();

  private Map<Instant, AnalysisPipeline> analysisPipelineMap = new HashMap<>();
  private Lock pipelineLock = new ReentrantLock();

  private TaskExecutor taskExecutor;

  @Autowired
  public AnalysisPipelineProcessorImpl(TaskExecutor taskExecutor) {
    this.taskExecutor = taskExecutor;
  }

  @EventListener
  public void onSPARQLReadyEvent(SPARQLDAOUpdatedEvent event) {
    logger.debug("Recognized a SPARQL update event {}.", event);
    pipelineLock.lock();
    try {
      Instant daoTimestamp = event.getDAOTimestamp();
      AnalysisPipeline analysisPipeline = analysisPipelineMap.get(daoTimestamp);
      if (analysisPipeline == null) {
        analysisPipeline = AnalysisPipeline
            .of(daoTimestamp, analysisServices.stream().map(Entry::deepCopy).collect(
                Collectors.toList()), taskExecutor);
        analysisPipelineMap.put(daoTimestamp, analysisPipeline);
      }
      analysisPipeline.registerAvailabilityOf(SPARQLService.class);
    } finally {
      pipelineLock.unlock();
    }
  }

  @EventListener
  public void onSPARQLReadyEvent(FullTextSearchDAOUpdatedEvent event) {
    logger.debug("Recognized a FullTextSearch update event {}.", event);
    pipelineLock.lock();
    try {
      Instant daoTimestamp = event.getDAOTimestamp();
      AnalysisPipeline analysisPipeline = analysisPipelineMap.get(daoTimestamp);
      if (analysisPipeline == null) {
        analysisPipeline = AnalysisPipeline
            .of(daoTimestamp, analysisServices.stream().map(Entry::deepCopy).collect(
                Collectors.toList()), taskExecutor);
        analysisPipelineMap.put(daoTimestamp, analysisPipeline);
      }
      analysisPipeline.registerAvailabilityOf(FullTextSearchService.class);
    } finally {
      pipelineLock.unlock();
    }
  }

  @EventListener
  public void onSPARQLReadyEvent(GremlinDAOUpdatedEvent event) {
    logger.debug("Recognized a Gremlin update event {}.", event);
    pipelineLock.lock();
    try {
      Instant daoTimestamp = event.getDAOTimestamp();
      AnalysisPipeline analysisPipeline = analysisPipelineMap.get(daoTimestamp);
      if (analysisPipeline == null) {
        analysisPipeline = AnalysisPipeline
            .of(daoTimestamp, analysisServices.stream().map(Entry::deepCopy).collect(
                Collectors.toList()), taskExecutor);
        analysisPipelineMap.put(daoTimestamp, analysisPipeline);
      }
      analysisPipeline.registerAvailabilityOf(GremlinService.class);
    } finally {
      pipelineLock.unlock();
    }
  }

  /**
   * Registers the given {@code analysisService} with the given {@code requirements}.
   *
   * @param analysisService {@link AnalysisService} that shall be registered.
   * @param requiresSPARQL {@code true}, if {@link SPARQLService} is required, otherwise {@code
   * false}.
   * @param requiresFTS {@code true}, if {@link FullTextSearchService} is required, otherwise {@code
   * false}.
   * @param requiresGremlin {@code true}, if {@link GremlinService} is required, otherwise {@code
   * false}.
   * @param requirements {@link Class} representing analysis services that are required by the given
   * {@code analysisService}.
   */
  @Override
  public void registerAnalysisService(AnalysisService analysisService,
      boolean requiresSPARQL, boolean requiresFTS, boolean requiresGremlin,
      Set<Class<? extends AnalysisService>> requirements) {
    Set<Class<?>> combinedRequirements = new HashSet<>(requirements != null ? requirements : Sets
        .newHashSet());
    if (requiresSPARQL) {
      combinedRequirements.add(SPARQLService.class);
    }
    if (requiresFTS) {
      combinedRequirements.add(FullTextSearchService.class);
    }
    if (requiresGremlin) {
      combinedRequirements.add(GremlinService.class);
    }
    logger.debug("Registers analysis service {} with requirements {}.", analysisService,
        combinedRequirements);
    analysisServices.add(new Entry(analysisService, combinedRequirements));
  }

}
