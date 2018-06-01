package at.ac.tuwien.ifs.es.middleware.dao.stardog.conf;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.stardog.StardogKnowledgeGraphDAO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * This conf is for Stardog instances that are running on a remote location.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://www.stardog.com/docs/">Stardog</a>
 * @since 1.0
 */
@Configuration("Stardog")
public class StardogKnowledgeGraphDAOConfig implements KnowledgeGraphDAOConfig {

  private ApplicationContext context;

  public StardogKnowledgeGraphDAOConfig(ApplicationContext context) {
    this.context = context;
  }

  @Override
  public KGSparqlDAO getSparqlDAO() {
    return context.getBean(StardogKnowledgeGraphDAO.class);
  }

  @Override
  public KGFullTextSearchDAO getFullTextSearchDAO() {
    return context.getBean(StardogKnowledgeGraphDAO.class);
  }

  @Override
  public KGGremlinDAO getGremlinDAO() {
    //TODO: Implement
    return null;
  }
}
