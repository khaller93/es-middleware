package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import at.ac.tuwien.ifs.es.middleware.dao.graphdb.lucene.Lucene;
import at.ac.tuwien.ifs.es.middleware.dao.graphdb.lucene.legacy.LegacyLucene;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.KGGeneralDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is a configuration for GraphDB.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://ontotext.com/products/graphdb/">Ontotext GraphDB</a>
 * @since 1.0
 */
@Lazy
@Component("GraphDB")
public class GraphDbConfig extends
    KGGeneralDAOConfig<GraphDbSparqlDAO, KGFullTextSearchDAO, KGGremlinDAO> {

  private final ApplicationContext context;
  private final boolean legacyLucene;

  public GraphDbConfig(ApplicationContext context,
      @Value("${esm.db.sparql.choice:#{null}}") String sparqlChoice,
      @Value("${esm.db.fts.choice:#{null}}") String ftsChoice,
      @Value("${esm.db.gremlin.choice:#{null}}") String gremlinChoice,
      @Value("${graphdb.lucene.legacy:#{false}}") boolean legacyLucene) {
    super(context, sparqlChoice, ftsChoice, gremlinChoice);
    this.context = context;
    this.legacyLucene = legacyLucene;
  }

  @Override
  protected GraphDbSparqlDAO getDefaultSparqlDAO() {
    return context.getBean(RemoteGraphDbDAO.class);
  }

  @Override
  protected Class<GraphDbSparqlDAO> getSparqlDAOClass() {
    return GraphDbSparqlDAO.class;
  }

  @Override
  protected KGFullTextSearchDAO getDefaultFullTextSearchDAO() {
    return this.legacyLucene ? context.getBean(LegacyLucene.class) : context.getBean(Lucene.class);
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
