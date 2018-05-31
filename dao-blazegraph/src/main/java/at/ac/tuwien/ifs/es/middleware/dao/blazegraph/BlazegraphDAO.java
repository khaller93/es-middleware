package at.ac.tuwien.ifs.es.middleware.dao.blazegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JKnowledgeGraphDAO;
import javax.annotation.PostConstruct;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
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
 * This class is an implementation of {@link KnowledgeGraphDAO} for blazegraph triplestore.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://www.blazegraph.com/">Blazegraph</a>
 * @since 1.0
 */
@Lazy
@Component("Blazegraph")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BlazegraphDAO extends RDF4JKnowledgeGraphDAO {

  private static final Logger logger = LoggerFactory.getLogger(BlazegraphDAO.class);

  @Value("${blazegraph.queryEndpointURL}")
  private String queryEndpointURL;

  @Autowired
  public BlazegraphDAO(ApplicationContext context) {
    super(context);
  }

  @PostConstruct
  public void setUp() {
    init(new SPARQLRepository(queryEndpointURL));
  }

  @Override
  public KGFullTextSearchDAO getFullTextSearchDAO() {
    return null;
  }

  @Override
  public KGGremlinDAO getGremlinDAO() {
    return null;
  }
}
