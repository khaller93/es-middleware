package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JKnowledgeGraphDAO;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

/**
 * An implementation of {@link KnowledgeGraphDAO} for the Ontotext GraphDB triplestore.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://ontotext.com/products/graphdb/">Ontotext GraphDB</a>
 * @since 1.0
 */
public abstract class GraphDbDAO extends RDF4JKnowledgeGraphDAO {

  private static final Logger logger = LoggerFactory.getLogger(GraphDbDAO.class);

  private ApplicationContext context;

  @Value("${graphdb.fts.choice:#{null}}")
  private String fullTextSearchChoice;

  protected void initGraphDb(Repository repository, ApplicationContext context) {
    this.init(repository);
    this.context = context;
  }

  @Override
  public FullTextSearchDAO getFullTextSearchDAO() {
    if (fullTextSearchChoice == null) {
      return context.getBean("InBuiltLucene", FullTextSearchDAO.class);
    } else {
      return context.getBean(fullTextSearchChoice, FullTextSearchDAO.class);
    }
  }

  @Override
  public KnowledgeGraphGremlinDAO getGremlinDAO() {
    return context.getBean("InMemoryGremlin", KnowledgeGraphGremlinDAO.class);
  }
}
