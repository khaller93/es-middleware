package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.sparql.QueryResult;
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
    return sparqlDAO.query(query, includeInference);
  }

  @Override
  public void update(String query) throws KGSPARQLException {
    sparqlDAO.update(query);
  }
}
