package at.ac.tuwien.ifs.es.middleware.service.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link SPARQLService} that caches the query requests using Redis.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service("CachedSPARQLService")
public class CachedSPARQLService implements SPARQLService {

  private KnowledgeGraphDAO knowledgeGraphDAO;

  public CachedSPARQLService(@Autowired KnowledgeGraphDAO knowledgeGraphDAO) {
    this.knowledgeGraphDAO = knowledgeGraphDAO;
  }

  @Cacheable({"sparql","query"})
  @Override
  public QueryResult query(String query, boolean includeInference)
      throws KnowledgeGraphSPARQLException {
    return knowledgeGraphDAO.query(query, includeInference);
  }

  @Override
  public void update(String query) throws KnowledgeGraphSPARQLException {
    knowledgeGraphDAO.update(query);
  }
}
