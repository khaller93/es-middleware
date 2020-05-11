package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.LuceneIndexedRDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOSetupException;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet.FacetFilter;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.facet.FacetedSearchQueryBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("RDF4JLucene")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RDF4JLuceneFullTextSearchDAO implements KGFullTextSearchDAO {

  private static final Logger logger = LoggerFactory.getLogger(RDF4JLuceneFullTextSearchDAO.class);

  private final static String FTS_QUERY =
      "PREFIX search: <http://www.openrdf.org/contrib/lucenesail#>\n"
          + "SELECT ?resource ?score\n"
          + "WHERE\n"
          + "{\n"
          + "  ?resource search:matches [\n"
          + "    search:query \"${keyword}\";\n"
          + "    search:score ?score\n"
          + "  ] .\n"
          + "  ${body}\n"
          + "} ORDER BY DESC(?score)\n"
          + "${offset}\n"
          + "${limit}\n";

  private KGSparqlDAO sparqlDAO;

  @Autowired
  public RDF4JLuceneFullTextSearchDAO(@Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO) {
    this.sparqlDAO = sparqlDAO;
  }

  @Override
  public void setup() throws KGDAOException {
    if (!(sparqlDAO instanceof LuceneIndexedRDF4JSparqlDAO)) {
      String message = String.format(
          "The given SPARQL DAO must be indexed with Lucene and implement the '%s' interface.",
          RDF4JLuceneFullTextSearchDAO.class.getName());
      throw new KGDAOSetupException(message);
    }
  }

  @Override
  public void update(long timestamp) throws KGDAOException {
    //nothing to do
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit) {
    return searchFullText(keyword, classes, offset, limit, null);
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit, List<FacetFilter> facets) throws KGDAOException {
    logger
        .debug("FTS call for {} was triggered with parameters: offset={}, limit={}, and classes={}",
            keyword, offset, limit, classes);
    Map<String, String> valueMap = new HashMap<>();
    valueMap.put("keyword", keyword);
    /* windowing */
    valueMap.put("offset", offset != null ? "OFFSET " + offset.toString() : "");
    valueMap.put("limit", limit != null ? "LIMIT " + limit.toString() : "");
    /* build facets */
    FacetedSearchQueryBuilder queryBuilder = FacetedSearchQueryBuilder.forSubject("resource");
    queryBuilder.includeInstancesOfClasses(classes);
    if (facets != null) {
      facets.forEach(queryBuilder::addPropertyFacet);
    }
    valueMap.put("body", queryBuilder.getQueryBody());
    String filledFtsQuery = new StringSubstitutor(valueMap).replace(FTS_QUERY);
    return sparqlDAO.<SelectQueryResult>query(filledFtsQuery, true).value();
  }

}
