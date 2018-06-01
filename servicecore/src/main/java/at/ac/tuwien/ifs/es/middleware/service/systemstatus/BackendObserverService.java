package at.ac.tuwien.ifs.es.middleware.service.systemstatus;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOFailedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dto.status.BackendServiceStatus;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * This service allows the client to analyze the provided features, abilities as well as the health
 * status of the middleware.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BackendObserverService {

  private static final Logger logger = LoggerFactory.getLogger(BackendObserverService.class);

  public static final String SPARQL_DAO = "dao.sparql";
  public static final String FULLTEXTSEARCH_DAO = "dao.fulltextsearch";
  public static final String Gremlin_DAO = "dao.gremlin";

  private Map<String, BackendServiceStatus> backendServiceStatusMap = new HashMap<>();

  public BackendObserverService() {
    backendServiceStatusMap.put(SPARQL_DAO, BackendServiceStatus.initiating());
    backendServiceStatusMap.put(FULLTEXTSEARCH_DAO, BackendServiceStatus.initiating());
    backendServiceStatusMap.put(Gremlin_DAO, BackendServiceStatus.initiating());
  }

  @EventListener
  public void handleSparqlDAOReady(SPARQLDAOReadyEvent event) {
    logger.info("Detected a state change to 'READY' of SPARQL DAO {}.", event.getSource());
    backendServiceStatusMap.put(SPARQL_DAO, BackendServiceStatus.ready());
  }

  @EventListener
  public void handleSparqlDAOFailed(SPARQLDAOFailedEvent event) {
    logger
        .info("Detected a state change to 'FAILED' of SPARQL DAO {}. Error '{}' with exception {}.",
            event.getSource(), event.getMessage(), event.getException());
    backendServiceStatusMap.put(SPARQL_DAO, BackendServiceStatus.failed(event.getMessage()));
  }

  @EventListener
  public void handleSparqlDAOUpdating(SPARQLDAOFailedEvent event) {
    logger.info("Detected a state change to 'UPDATING' of SPARQL DAO {}.", event.getSource());
    backendServiceStatusMap.put(SPARQL_DAO, BackendServiceStatus.updating());
  }

  @EventListener
  public void handleSparqlDAOUpdated(SPARQLDAOFailedEvent event) {
    logger.info("Detected a state change to 'UPDATED' of SPARQL DAO {}.", event.getSource());
    backendServiceStatusMap.put(SPARQL_DAO, BackendServiceStatus.ready());
  }

  /**
   * Returns the currently known status of each DAO in form of a map.
   *
   * @return the currently known status of each DAO in form of a map.
   */
  public Map<String, BackendServiceStatus> getBackendServiceStatusMap() {
    return backendServiceStatusMap;
  }
}
