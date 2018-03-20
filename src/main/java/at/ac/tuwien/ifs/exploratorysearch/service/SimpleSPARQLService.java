package at.ac.tuwien.ifs.exploratorysearch.service;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.SPARQLExecutionException;
import at.ac.tuwien.ifs.exploratorysearch.dto.sparql.QueryResult;
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
@Service
public class SimpleSPARQLService implements SPARQLService {

  private static final Logger logger = LoggerFactory.getLogger(SimpleSPARQLService.class);

  private KnowledgeGraphDAO knowledgeGraphDAO;

  public SimpleSPARQLService(
      @Autowired @Qualifier("SpecifiedKnowledgeGraphDAO") KnowledgeGraphDAO knowledgeGraphDAO) {
    this.knowledgeGraphDAO = knowledgeGraphDAO;
  }

  @Override
  public QueryResult query(String query, boolean includeInference) throws SPARQLExecutionException {
    QueryResult queryResult = knowledgeGraphDAO.query(query, includeInference);
    logger.info("SPARQL Query '{}' execution returned result: {}", query, queryResult);
    return queryResult;
  }
}
