package at.ac.tuwien.ifs.es.middleware.service.analysis;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.FTSDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.GremlinDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.SparqlDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus.CODE;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.fts.FullTextSearchService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
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

  private final Map<Long, AnalysisPipeline> analysisPipelineMap = new HashMap<>();

  private final AnalysisServiceRegistry registry;

  private final KGSparqlDAO sparqlDAO;
  private final KGGremlinDAO gremlinDAO;
  private final KGFullTextSearchDAO fullTextSearchDAO;

  private final TaskExecutor taskExecutor;
  private final Lock pipelineLock = new ReentrantLock();

  @Value("${esm.analysis.computeOnStart:#{false}}")
  private boolean computeAnalysisOnStart;

  @Autowired
  public AnalysisPipelineProcessorImpl(AnalysisServiceRegistry registry,
      @Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      @Qualifier("getGremlinDAO") KGGremlinDAO gremlinDAO,
      @Qualifier("getFullTextSearchDAO") KGFullTextSearchDAO fullTextSearchDAO,
      TaskExecutor taskExecutor) {
    this.registry = registry;
    this.taskExecutor = taskExecutor;
    this.sparqlDAO = sparqlDAO;
    this.gremlinDAO = gremlinDAO;
    this.fullTextSearchDAO = fullTextSearchDAO;
  }

  @EventListener
  public void onApplicationEvent(ApplicationReadyEvent event) {
    registry.scanAndRegisterAnalysisServices(event.getApplicationContext());
    logger.debug("Start to register analysis services.");
    if (computeAnalysisOnStart) {
      logger.info("Analysis will be computed at startup.");
      if (CODE.READY.equals(sparqlDAO.getStatus().getCode())) {
        informPipelineAboutChange(-1L, SPARQLService.class);
      }
      if (CODE.READY.equals(gremlinDAO.getStatus().getCode())) {
        informPipelineAboutChange(-1L, GremlinService.class);
      }
      if (CODE.READY.equals(fullTextSearchDAO.getStatus().getCode())) {
        informPipelineAboutChange(-1L, FullTextSearchService.class);
      }
    }
  }

  @EventListener
  public void onSPARQLReadyEvent(SparqlDAOStateChangeEvent event) {
    logger.debug("Recognized a SPARQL DAO change event {}.", event);
    if (CODE.READY.equals(event.getStatus().getCode())) {
      informPipelineAboutChange(event.getEventId(), SPARQLService.class);
    } else {
      logger.debug("SPARQL DAO state change event {} was ignored.", event);
    }
  }

  @EventListener
  public void onFTSReadyEvent(FTSDAOStateChangeEvent event) {
    logger.debug("Recognized a FullTextSearch DAO change event {}.", event);
    if (CODE.READY.equals(event.getStatus().getCode())) {
      informPipelineAboutChange(event.getEventId(), FullTextSearchService.class);
    } else {
      logger.debug("SPARQL DAO state change event {} was ignored.", event);
    }
  }

  @EventListener
  public void onGremlinReadyEvent(GremlinDAOStateChangeEvent event) {
    logger.debug("Recognized a Gremlin DAO change event {}.", event);
    if (CODE.READY.equals(event.getStatus().getCode())) {
      informPipelineAboutChange(event.getEventId(), GremlinService.class);
    } else {
      logger.debug("Gremlin DAO state change event {} was ignored.", event);
    }
  }

  /**
   *
   */
  private void informPipelineAboutChange(long eventId, Class<?> serviceClass) {
    pipelineLock.lock();
    try {
      AnalysisPipeline analysisPipeline = analysisPipelineMap.get(eventId);
      if (analysisPipeline == null) {
        analysisPipeline = AnalysisPipeline
            .of(eventId, registry.getRegisteredAnalysisServices(), taskExecutor);
        analysisPipelineMap.put(eventId, analysisPipeline);
      }
      analysisPipeline.registerAvailabilityOf(serviceClass);
    } finally {
      pipelineLock.unlock();
    }
  }
}
