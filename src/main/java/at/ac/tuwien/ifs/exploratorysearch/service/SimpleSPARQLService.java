package at.ac.tuwien.ifs.exploratorysearch.service;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.exploratorysearch.dto.sparql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This is a simple implementation of {@link SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class SimpleSPARQLService implements SPARQLService {

  private KnowledgeGraphDAO knowledgeGraphDAO;

  public SimpleSPARQLService(
      @Autowired KnowledgeGraphDAO knowledgeGraphDAO) {
    this.knowledgeGraphDAO = knowledgeGraphDAO;
  }

  @Override
  public QueryResult query(String query, boolean includeInference) throws SPARQLExecutionException {
    return knowledgeGraphDAO.query(query, includeInference);
  }
}
