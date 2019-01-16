package at.ac.tuwien.ifs.es.middleware.service.analysis;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.DAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.FTSDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.GremlinDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.SparqlDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus.CODE;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.fts.FullTextSearchService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  private ConcurrentLinkedQueue<Entry> analysisServices;
  private Map<Long, AnalysisPipeline> analysisPipelineMap;

  private Lock pipelineLock;
  private TaskExecutor taskExecutor;

  private KGSparqlDAO sparqlDAO;
  private KGGremlinDAO gremlinDAO;
  private KGFullTextSearchDAO fullTextSearchDAO;
  @Value("${esm.analysis.computeOnStart:#{false}}")
  private boolean computeAnalysisOnStart;

  @Autowired
  public AnalysisPipelineProcessorImpl(TaskExecutor taskExecutor, KGSparqlDAO sparqlDAO,
      KGGremlinDAO gremlinDAO, KGFullTextSearchDAO fullTextSearchDAO) {
    this.taskExecutor = taskExecutor;
    this.sparqlDAO = sparqlDAO;
    this.gremlinDAO = gremlinDAO;
    this.fullTextSearchDAO = fullTextSearchDAO;
    this.analysisPipelineMap = new HashMap<>();
    this.analysisServices = new ConcurrentLinkedQueue<>();
    this.pipelineLock = new ReentrantLock();
  }

  @PostConstruct
  public void setUp() {
    logger.info("!>>>> {}. SPARQL DAO: {}, Gremlin DAO: {}, FTS DAO: {}", computeAnalysisOnStart,
        sparqlDAO.getStatus().getCode(), gremlinDAO.getStatus().getCode(),
        fullTextSearchDAO.getStatus().getCode());
    if (computeAnalysisOnStart) {
      logger.info("Analysis will be computed at startup.");
      //TODO: think about safe pipeline.
      taskExecutor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(30000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
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
      });
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

  private void informPipelineAboutChange(long eventId, Class<?> serviceClass) {
    pipelineLock.lock();
    try {
      AnalysisPipeline analysisPipeline = analysisPipelineMap.get(eventId);
      if (analysisPipeline == null) {
        analysisPipeline = AnalysisPipeline
            .of(eventId, analysisServices.stream().map(Entry::deepCopy)
                .collect(Collectors.toList()), taskExecutor);
        analysisPipelineMap.put(eventId, analysisPipeline);
      }
      analysisPipeline.registerAvailabilityOf(serviceClass);
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
