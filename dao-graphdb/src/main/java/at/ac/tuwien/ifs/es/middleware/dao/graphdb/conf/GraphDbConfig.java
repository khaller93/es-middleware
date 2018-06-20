package at.ac.tuwien.ifs.es.middleware.dao.graphdb.conf;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.SPARQLSyncingGremlinDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

/**
 * This is a conf of a GraphDB instance.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class GraphDbConfig implements KnowledgeGraphDAOConfig {

  @Value("${graphdb.fts.choice:#{null}}")
  private String fullTextSearchChoice;
  @Value("${esm.db.gremlin.choice:#{null}}")
  private String gremlinChoice;

  private ApplicationContext context;

  public GraphDbConfig(ApplicationContext context) {
    this.context = context;
  }

  public ApplicationContext getContext() {
    return context;
  }

  @Override
  public KGFullTextSearchDAO getFullTextSearchDAO() {
    if (fullTextSearchChoice == null) {
      return getContext().getBean("InBuiltLucene", KGFullTextSearchDAO.class);
    } else {
      return getContext().getBean(fullTextSearchChoice, KGFullTextSearchDAO.class);
    }
  }

  @Override
  public KGGremlinDAO getGremlinDAO() {
    return context.getBean(
        gremlinChoice != null && !gremlinChoice.isEmpty() ? gremlinChoice : "InMemoryGremlin",
        SPARQLSyncingGremlinDAO.class);
  }

}
