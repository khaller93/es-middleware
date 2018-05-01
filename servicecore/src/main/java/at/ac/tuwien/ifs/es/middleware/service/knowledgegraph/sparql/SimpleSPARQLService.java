package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.MalformedSPARQLQueryException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * This is a simple implementation of {@link SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Service("SimpleSPARQLService")
public class SimpleSPARQLService implements SPARQLService {

  private KnowledgeGraphDAO KnowledgeGraphDAO;

  public SimpleSPARQLService(@Autowired KnowledgeGraphDAO KnowledgeGraphDAO) {
    this.KnowledgeGraphDAO = KnowledgeGraphDAO;
  }

  @Override
  public QueryResult query(String query, boolean includeInference) throws SPARQLExecutionException,
      MalformedSPARQLQueryException {
    return KnowledgeGraphDAO.query(query, includeInference);
  }

  @Override
  public void update(String query) throws SPARQLExecutionException,
      MalformedSPARQLQueryException {
    KnowledgeGraphDAO.update(query);
  }
}
