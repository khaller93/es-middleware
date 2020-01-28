package at.ac.tuwien.ifs.es.middleware.service.systemstatus;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * This service allows the client to analyze the provided features, abilities as well as the health
 * status get the middleware.
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

  private KGSparqlDAO sparqlDAO;
  private KGFullTextSearchDAO fullTextSearchDAO;
  private KGGremlinDAO gremlinDAO;

  @Autowired
  public BackendObserverService(@Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      @Qualifier("getFullTextSearchDAO") KGFullTextSearchDAO fullTextSearchDAO,
      @Qualifier("getGremlinDAO") KGGremlinDAO gremlinDAO) {
    this.sparqlDAO = sparqlDAO;
    this.fullTextSearchDAO = fullTextSearchDAO;
    this.gremlinDAO = gremlinDAO;
  }

  /**
   * Returns the currently known status get each DAO in form get a map.
   *
   * @return the currently known status get each DAO in form get a map.
   */
  public Map<String, KGDAOStatus> getBackendServiceStatusMap() {
    Map<String, KGDAOStatus> backendServiceStatusMap = new HashMap<>();
 //   backendServiceStatusMap.put(SPARQL_DAO, sparqlDAO.getStatus());
 //   backendServiceStatusMap.put(FULLTEXTSEARCH_DAO, fullTextSearchDAO.getStatus());
 //   backendServiceStatusMap.put(Gremlin_DAO, gremlinDAO.getStatus());
    return backendServiceStatusMap;
  }
}
