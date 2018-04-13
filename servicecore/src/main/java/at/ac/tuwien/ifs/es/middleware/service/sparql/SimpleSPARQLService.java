package at.ac.tuwien.ifs.es.middleware.service.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.MalformedSPARQLQueryException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This is a simple implementation of {@link SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service("SimpleSPARQLService")
public class SimpleSPARQLService implements SPARQLService {

  private static final Logger logger = LoggerFactory.getLogger(SimpleSPARQLService.class);

  private KnowledgeGraphDAO knowledgeGraphDAO;

  public SimpleSPARQLService(
      @Autowired @Qualifier("SpecifiedKnowledgeGraphDAO") KnowledgeGraphDAO knowledgeGraphDAO) {
    this.knowledgeGraphDAO = knowledgeGraphDAO;
  }

  @Override
  public QueryResult query(String query, boolean includeInference) throws SPARQLExecutionException,
      MalformedSPARQLQueryException {
    return knowledgeGraphDAO.query(query, includeInference);
  }

  @Override
  public void update(String query) throws SPARQLExecutionException,
      MalformedSPARQLQueryException {
    knowledgeGraphDAO.update(query);
  }
}
