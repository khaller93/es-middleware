package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGeneralDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JSparqlDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is a configuration for RDF4J stores.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("RDF4J")
public class RDF4JDAOConfig extends
    KGGeneralDAOConfig<RDF4JSparqlDAO, KGFullTextSearchDAO, KGGremlinDAO> {

  private ApplicationContext context;

  @Autowired
  public RDF4JDAOConfig(ApplicationContext context,
      @Value("${esm.db.sparql.choice:#{null}}") String sparqlChoice,
      @Value("${esm.db.fts.choice:#{null}}") String ftsChoice,
      @Value("${esm.db.gremlin.choice:#{null}}") String gremlinChoice) {
    super(context, sparqlChoice, ftsChoice, gremlinChoice);
    this.context = context;
  }

  @Override
  protected RDF4JSparqlDAO getDefaultSparqlDAO() {
    return context.getBean(RDF4JMemoryStoreWithLuceneSparqlDAO.class);
  }

  @Override
  protected Class<RDF4JSparqlDAO> getSparqlDAOClass() {
    return RDF4JSparqlDAO.class;
  }

  @Override
  protected KGFullTextSearchDAO getDefaultFullTextSearchDAO() {
    return context.getBean(RDF4JLuceneFullTextSearchDAO.class);
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
