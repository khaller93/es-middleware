package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.fts;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphDAOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * This class is an implementation of {@link FullTextSearchService} that caches the full-text-search
 * result.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Service("CachedFullTextSearchService")
public class CachedFullTextSearchService implements FullTextSearchService {

  private FullTextSearchDAO fullTextSearchDAO;

  public CachedFullTextSearchService(@Autowired KnowledgeGraphDAO knowledgeGraphDAO) {
    this.fullTextSearchDAO = knowledgeGraphDAO.getFullTextSearchDAO();
  }

  @Cacheable({"fts"})
  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword) {
    return fullTextSearchDAO.searchFullText(keyword);
  }

  @Cacheable({"fts"})
  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes) {
    return fullTextSearchDAO.searchFullText(keyword, classes);
  }

  @Cacheable({"fts"})
  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit) throws KnowledgeGraphDAOException {
    return fullTextSearchDAO.searchFullText(keyword, classes, offset, limit);
  }
}