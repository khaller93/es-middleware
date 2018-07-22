package at.ac.tuwien.ifs.es.middleware.dao.graphdb.conf;

import at.ac.tuwien.ifs.es.middleware.dao.graphdb.GraphDbLucene;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.InMemoryGremlinDAO;
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

  private String fullTextSearchChoice;
  private String gremlinChoice;

  private ApplicationContext context;

  public GraphDbConfig(ApplicationContext context, String fullTextSearchChoice,
      String gremlinChoice) {
    this.fullTextSearchChoice = fullTextSearchChoice;
    this.gremlinChoice = gremlinChoice;
    this.context = context;
  }

  public ApplicationContext getContext() {
    return context;
  }

  @Override
  public KGFullTextSearchDAO getFullTextSearchDAO() {
    if (fullTextSearchChoice == null || fullTextSearchChoice.isEmpty()) {
      return getContext().getBean(GraphDbLucene.class);
    } else {
      return getContext().getBean(fullTextSearchChoice, KGFullTextSearchDAO.class);
    }
  }

  @Override
  public KGGremlinDAO getGremlinDAO() {
    if (gremlinChoice == null || gremlinChoice.isEmpty()) {
      return getContext().getBean(InMemoryGremlinDAO.class);
    } else {
      return getContext().getBean(gremlinChoice, SPARQLSyncingGremlinDAO.class);
    }
  }

}
