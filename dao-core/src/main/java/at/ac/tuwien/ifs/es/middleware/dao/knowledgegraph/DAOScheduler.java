package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.KGUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.scheduler.ScheduleTask;
import at.ac.tuwien.ifs.es.middleware.scheduler.SchedulerPipeline;
import at.ac.tuwien.ifs.es.middleware.scheduler.behaviour.NRetryBehaviour;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * This class handles the scheduling of DAO and analysis tasks.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Component
public class DAOScheduler {

  private static final String SPARQL_SERVICE_ID = "at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SPARQLService";
  private static final String GREMLIN_SERVICE_ID = "at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.GremlinService";
  private static final String FTS_SERVICE_ID = "at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.FullTextSearchService";

  private final SchedulerPipeline schedulerPipeline;

  private final KGSparqlDAO sparqlDAO;
  private final KGGremlinDAO gremlinDAO;
  private final KGFullTextSearchDAO fullTextSearchDAO;

  private final DAODependencyGraphService daoDependencyGraphService;
  private final TaskExecutor threadpool;

  @Value("${esm.db.syncOnStart:#{false}}")
  private boolean syncOnStart;
  @Value("${esm.db.setup.delay:#{0}}")
  private long delayDAOInMs;

  @Autowired
  public DAOScheduler(SchedulerPipeline schedulerPipeline,
      @Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      @Qualifier("getGremlinDAO") KGGremlinDAO gremlinDAO,
      @Qualifier("getFullTextSearchDAO") KGFullTextSearchDAO fullTextSearchDAO,
      DAODependencyGraphService daoDependencyGraphService,
      TaskExecutor threadPool) {
    this.schedulerPipeline = schedulerPipeline;
    this.sparqlDAO = sparqlDAO;
    this.gremlinDAO = gremlinDAO;
    this.fullTextSearchDAO = fullTextSearchDAO;
    this.daoDependencyGraphService = daoDependencyGraphService;
    this.threadpool = threadPool;
  }

  @EventListener
  public void onApplicationEvent(ApplicationReadyEvent event) {
    pushTasks(syncOnStart ? Instant.now().toEpochMilli() : -1, delayDAOInMs);
  }

  private void pushTasks(long daoTimestamp, long delay) {
    threadpool.execute(() -> {
      if (delay > 0) {
        try {
          Thread.sleep(delay);
        } catch (InterruptedException e) {
        }
      }
      schedulerPipeline.pushTasks(Lists.newArrayList(
          new ScheduleTask(KGSparqlDAO.class.getName(), daoTimestamp, sparqlDAO::setup,
              daoDependencyGraphService.getSPARQLRequirements(),
              Collections.singleton(SPARQL_SERVICE_ID), NRetryBehaviour.of(10)),
          new ScheduleTask(KGGremlinDAO.class.getName(), daoTimestamp, gremlinDAO::setup,
              daoDependencyGraphService.getGremlinRequirements(),
              Collections.singleton(GREMLIN_SERVICE_ID), NRetryBehaviour.of(10)),
          new ScheduleTask(KGFullTextSearchDAO.class.getName(), daoTimestamp,
              fullTextSearchDAO::setup, daoDependencyGraphService.getFTSRequirements(),
              Collections.singleton(FTS_SERVICE_ID), NRetryBehaviour.of(10))
      ));
    });
  }

  @EventListener
  public void onKnowledgeGraphUpdated(KGUpdatedEvent updatedEvent) {
    pushTasks(updatedEvent.getTimestamp(), 0);
  }

}
