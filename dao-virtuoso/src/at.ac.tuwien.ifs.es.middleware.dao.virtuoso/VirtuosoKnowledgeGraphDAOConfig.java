package at.ac.tuwien.ifs.es.middleware.dao.virtuoso;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.KGGeneralDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Virtuoso database.
 * <p/>
 * Those default choices can be overwritten by using the corresponding application properties.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="http://vos.openlinksw.com/owiki/wiki/VOS">OpenLink Virtuoso</a>
 * @since 1.0
 */
@Configuration("Virtuoso")
public class VirtuosoKnowledgeGraphDAOConfig extends
    KGGeneralDAOConfig<VirtuosoKnowledgeGraphDAO, KGFullTextSearchDAO, KGGremlinDAO> {

  private ApplicationContext context;

  @Autowired
  public VirtuosoKnowledgeGraphDAOConfig(ApplicationContext context,
      @Value("${esm.db.sparql.choice:#{null}}") String sparqlChoice,
      @Value("${esm.db.fts.choice:#{null}}") String ftsChoice,
      @Value("${esm.db.gremlin.choice:#{null}}") String gremlinChoice) {
    super(context, sparqlChoice, ftsChoice, gremlinChoice);
    this.context = context;
  }

  @Override
  protected VirtuosoKnowledgeGraphDAO getDefaultSparqlDAO() {
    return context.getBean(VirtuosoKnowledgeGraphDAO.class);
  }

  @Override
  protected Class<VirtuosoKnowledgeGraphDAO> getSparqlDAOClass() {
    return VirtuosoKnowledgeGraphDAO.class;
  }

  @Override
  protected KGFullTextSearchDAO getDefaultFullTextSearchDAO() {
    return context.getBean(VirtuosoKnowledgeGraphDAO.class);
  }

  @Override
  protected Class<KGFullTextSearchDAO> getFullTextSearchDAOClass() {
    return KGFullTextSearchDAO.class;
  }

  @Override
  protected KGGremlinDAO getDefaultGremlinDAO() {
    return context.getBean(ClonedInMemoryGremlinDAO.class);
  }

  @Override
  protected Class<KGGremlinDAO> getGremlinDAOClass() {
    return KGGremlinDAO.class;
  }
}
