package at.ac.tuwien.ifs.es.middleware.service.fts;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphDAOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * This class is a simple implementation of {@link FullTextSearchService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Service("SimpleFullTextSearchService")
public class SimpleFullTextSearchService implements FullTextSearchService {

  private FullTextSearchDAO fullTextSearchDAO;

  public SimpleFullTextSearchService(
      @Autowired @Qualifier("getSpecificFullTextSearchDAO") FullTextSearchDAO fullTextSearchDAO) {
    this.fullTextSearchDAO = fullTextSearchDAO;
  }


  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword) {
    return fullTextSearchDAO.searchFullText(keyword);
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes) {
    return fullTextSearchDAO.searchFullText(keyword, classes);
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit) throws KnowledgeGraphDAOException {
    return fullTextSearchDAO.searchFullText(keyword, classes, offset, limit);
  }
}
