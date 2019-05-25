package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGMalformedSPARQLQueryException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLException;
import at.ac.tuwien.ifs.es.middleware.sparql.result.QueryResult;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.exception.SPARQLServiceExecutionException;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.exception.SPARQLServiceIllegalArgumentException;
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
      throws KGSPARQLException {
    if (query == null || query.isEmpty()) {
      throw new SPARQLServiceIllegalArgumentException(
          "The given query string must not be null or empty.");
    }
    try {
      return sparqlDAO.query(query, includeInference);
    } catch (KGMalformedSPARQLQueryException mf) {
      throw new SPARQLServiceIllegalArgumentException(mf);
    } catch (KGSPARQLException e) {
      throw new SPARQLServiceExecutionException(e);
    }
  }

  @Override
  public void update(String query) throws KGSPARQLException {
    if (query == null || query.isEmpty()) {
      throw new SPARQLServiceIllegalArgumentException(
          "The given query string must not be null or empty.");
    }
    try {
      sparqlDAO.update(query);
    } catch (KGMalformedSPARQLQueryException mf) {
      throw new SPARQLServiceIllegalArgumentException(mf);
    } catch (KGSPARQLException e) {
      throw new SPARQLServiceExecutionException(e);
    }
  }
}
