package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.GremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSetupException;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JKnowledgeGraphDAO;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@link KnowledgeGraphDAO} for the Ontotext GraphDB triplestore.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://ontotext.com/products/graphdb/">Ontotext GraphDB</a>
 * @since 1.0
 */
@Lazy
@Component("GraphDB")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GraphDbDAO extends RDF4JKnowledgeGraphDAO {

  private static final Logger logger = LoggerFactory.getLogger(GraphDbDAO.class);

  private ApplicationContext context;

  @Value("${graphdb.fts.choice:#{null}}")
  private String fullTextSearchChoice;

  /**
   * Creates a {@link GraphDbDAO} with the given location configuration.
   */
  public GraphDbDAO(@Value("${graphdb.address}") String address,
      @Value("${graphdb.repository.id}") String repositoryId,
      @Autowired ApplicationContext context) throws KnowledgeGraphSetupException {
    init(new HTTPRepository(address, repositoryId));
    this.context = context;
  }

  @Override
  public FullTextSearchDAO getFullTextSearchDAO() {
    if(fullTextSearchChoice == null){
      return context.getBean("InBuiltLucene", FullTextSearchDAO.class);
    } else {
     return context.getBean(fullTextSearchChoice, FullTextSearchDAO.class);
    }
  }

  @Override
  public GremlinDAO getGremlinDAO() {
    //TODO: Implement.
    return null;
  }
}
