package at.ac.tuwien.ifs.es.middleware.dao.blazegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGeneralDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This class is a {@link KGGeneralDAOConfig} for Blazegraph triplestore.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://wiki.blazegraph.com/wiki/index.php/Main_Page">Blazegraph</a>
 * @since 1.0
 */
@Lazy
@Component("Blazegraph")
public class BlazegraphDAOConfig extends
    KGGeneralDAOConfig<BlazegraphDAO, KGFullTextSearchDAO, KGGremlinDAO> {

  private ApplicationContext context;

  @Autowired
  public BlazegraphDAOConfig(ApplicationContext context,
      @Value("${esm.db.sparql.choice:#{null}}") String sparqlChoice,
      @Value("${esm.db.fts.choice:#{null}}") String ftsChoice,
      @Value("${esm.db.gremlin.choice:#{null}}") String gremlinChoice) {
    super(context, sparqlChoice, ftsChoice, gremlinChoice);
    this.context = context;
  }

  @Override
  protected BlazegraphDAO getDefaultSparqlDAO() {
    return context.getBean(BlazegraphDAO.class);
  }

  @Override
  protected Class<BlazegraphDAO> getSparqlDAOClass() {
    return BlazegraphDAO.class;
  }

  @Override
  protected KGFullTextSearchDAO getDefaultFullTextSearchDAO() {
    return context.getBean(BlazegraphDAO.class);
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
