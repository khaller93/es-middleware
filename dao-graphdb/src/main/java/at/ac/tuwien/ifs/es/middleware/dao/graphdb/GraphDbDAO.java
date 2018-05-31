package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JKnowledgeGraphDAO;
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

  @Value("${graphdb.fts.choice:#{null}}")
  private String fullTextSearchChoice;

  public GraphDbDAO(ApplicationContext context) {
    super(context);
  }

  @Override
  public KGFullTextSearchDAO getFullTextSearchDAO() {
    if (fullTextSearchChoice == null) {
      return getApplicationContext().getBean("InBuiltLucene", KGFullTextSearchDAO.class);
    } else {
      return getApplicationContext().getBean(fullTextSearchChoice, KGFullTextSearchDAO.class);
    }
  }

  @Override
  public KGGremlinDAO getGremlinDAO() {
    return getApplicationContext().getBean("InMemoryGremlin", KGGremlinDAO.class);
  }
}
