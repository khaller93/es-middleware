package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation get {@link SPARQLService} that caches the query requests.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Primary
@Service("SimpleSPARQLService")
public class SimpleSPARQLService implements SPARQLService {

  private KGSparqlDAO sparqlDAO;

  @Autowired
  public SimpleSPARQLService(@Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO) {
    this.sparqlDAO = sparqlDAO;
  }

  @Cacheable({"sparql"})
  @Override
  public <T extends QueryResult> T query(String query, boolean includeInference)
      throws KnowledgeGraphSPARQLException {
    return sparqlDAO.query(query, includeInference);
  }

  @Override
  public void update(String query) throws KnowledgeGraphSPARQLException {
    sparqlDAO.update(query);
  }
}
