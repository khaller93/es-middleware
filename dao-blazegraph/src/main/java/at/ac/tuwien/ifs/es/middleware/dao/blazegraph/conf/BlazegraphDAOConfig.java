package at.ac.tuwien.ifs.es.middleware.dao.blazegraph.conf;

import at.ac.tuwien.ifs.es.middleware.dao.blazegraph.BlazegraphDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component("Blazegraph")
public class BlazegraphDAOConfig implements KnowledgeGraphDAOConfig {

  private ApplicationContext context;

  @Autowired
  public BlazegraphDAOConfig(ApplicationContext context) {
    this.context = context;
  }

  @Override
  public KGSparqlDAO getSparqlDAO() {
    return context.getBean(BlazegraphDAO.class);
  }

  @Override
  public KGFullTextSearchDAO getFullTextSearchDAO() {
    //TODO: Implement
    return null;
  }

  @Override
  public KGGremlinDAO getGremlinDAO() {
    //TODO: Implement
    return null;
  }
}
