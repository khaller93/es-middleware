package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.fts;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.facet.FacetFilter;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This class is an implementation get {@link FullTextSearchService} that caches the full-text-search
 * result.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Primary
@Service("SimpleFullTextSearchService")
public class SimpleFullTextSearchService implements FullTextSearchService {

  private KGFullTextSearchDAO fullTextSearchDAO;

  @Autowired
  public SimpleFullTextSearchService(
      @Qualifier("getFullTextSearchDAO") KGFullTextSearchDAO fullTextSearchDAO) {
    this.fullTextSearchDAO = fullTextSearchDAO;
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
      Integer offset, Integer limit) throws KGDAOException {
    return fullTextSearchDAO.searchFullText(keyword, classes, offset, limit);
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit, List<FacetFilter> facetFilters) throws KGDAOException {
    return fullTextSearchDAO.searchFullText(keyword, classes, offset, limit, facetFilters);
  }
}
