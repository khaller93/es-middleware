package at.ac.tuwien.ifs.es.middleware.dao.stardog;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGeneralDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Stardog database. Per default, {@link StardogKnowledgeGraphDAO} accessible
 * remotely is used for SPARQL and Full-Text-Search. For Gremlin, the {@link ClonedInMemoryGremlinDAO}
 * will be used per default.
 * <p/>
 * Those default choices can be overwritten by using the corresponding application properties.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://www.stardog.com/docs/">Stardog</a>
 * @since 1.0
 */
@Configuration("Stardog")
public class StardogKnowledgeGraphDAOConfig extends
    KGGeneralDAOConfig<StardogKnowledgeGraphDAO, KGFullTextSearchDAO, KGGremlinDAO> {

  private ApplicationContext context;

  @Autowired
  public StardogKnowledgeGraphDAOConfig(ApplicationContext context,
      @Value("${esm.db.sparql.choice:#{null}}") String sparqlChoice,
      @Value("${esm.db.fts.choice:#{null}}") String ftsChoice,
      @Value("${esm.db.gremlin.choice:#{null}}") String gremlinChoice) {
    super(context, sparqlChoice, ftsChoice, gremlinChoice);
    this.context = context;
  }

  @Override
  protected StardogKnowledgeGraphDAO getDefaultSparqlDAO() {
    return context.getBean(StardogKnowledgeGraphDAO.class);
  }

  @Override
  protected Class<StardogKnowledgeGraphDAO> getSparqlDAOClass() {
    return StardogKnowledgeGraphDAO.class;
  }

  @Override
  protected KGFullTextSearchDAO getDefaultFullTextSearchDAO() {
    return context.getBean(StardogKnowledgeGraphDAO.class);
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
