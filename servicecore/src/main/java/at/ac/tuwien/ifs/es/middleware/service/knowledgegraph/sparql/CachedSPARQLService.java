package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link SPARQLService} that caches the query requests.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Service("CachedSPARQLService")
public class CachedSPARQLService implements SPARQLService {

  private KnowledgeGraphDAO KnowledgeGraphDAO;

  public CachedSPARQLService(@Autowired KnowledgeGraphDAO KnowledgeGraphDAO) {
    this.KnowledgeGraphDAO = KnowledgeGraphDAO;
  }

  @Cacheable({"sparql", "query"})
  @Override
  public QueryResult query(String query, boolean includeInference)
      throws KnowledgeGraphSPARQLException {
    return KnowledgeGraphDAO.query(query, includeInference);
  }

  @Override
  public void update(String query) throws KnowledgeGraphSPARQLException {
    KnowledgeGraphDAO.update(query);
  }
}
