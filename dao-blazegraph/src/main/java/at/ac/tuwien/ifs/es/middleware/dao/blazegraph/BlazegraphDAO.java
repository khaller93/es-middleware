package at.ac.tuwien.ifs.es.middleware.dao.blazegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet.FacetFilter;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.facet.FacetedSearchQueryBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of {@link at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO}
 * for blazegraph triplestore.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://www.blazegraph.com/">Blazegraph</a>
 * @since 1.0
 */
@Lazy
@Component("BlazegraphDAO")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BlazegraphDAO extends RDF4JSparqlDAO implements KGFullTextSearchDAO {

  private static final Logger logger = LoggerFactory.getLogger(BlazegraphDAO.class);

  private static final String FT_SEARCH_QUERY = "prefix bds: <http://www.bigdata.com/rdf/search#>\n"
      + "SELECT ?s (max(?litScore) as ?score) WHERE {\n"
      + "    _:lit bds:search \"shining\" .\n"
      + "    _:lit bds:matchAllTerms \"true\" .\n"
      + "    _:lit bds:relevance ?litScore .\n"
      + "    ?s ?p _:lit .\n"
      + "    ${body}\n"
      + "} GROUP BY(?s)\n"
      + "${offset}\n"
      + "${limit}";

  private String queryEndpointURL;

  @Autowired
  public BlazegraphDAO(ApplicationContext context,
      @Value("${blazegraph.address}") String queryEndpointURL) {
    this.queryEndpointURL = queryEndpointURL;
  }

  @PostConstruct
  public void setUp() {
    init(new SPARQLRepository(queryEndpointURL));
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit) throws KGDAOException {
    return searchFullText(keyword, classes, offset, limit, null);
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit, List<FacetFilter> facets) throws KGDAOException {
    logger.debug("Searching for '{}' of classes {} with limit={}, offset={}.", keyword, classes,
        offset, limit);
    Map<String, String> queryValuesMap = new HashMap<>();
    queryValuesMap.put("keyword", keyword);
    /* windowing */
    queryValuesMap.put("offset", offset != null ? String.format("OFFSET %d", offset) : "");
    queryValuesMap.put("limit", limit != null ? String.format("LIMIT %d", limit) : "");
    /* build facets */
    FacetedSearchQueryBuilder queryBuilder = FacetedSearchQueryBuilder.forSubject("s");
    queryBuilder.includeInstancesOfClasses(classes);
    if (facets != null) {
      facets.forEach(queryBuilder::addPropertyFacet);
    }
    queryValuesMap.put("body", queryBuilder.getQueryBody());
    String searchQuery = new StringSubstitutor(queryValuesMap).replace(FT_SEARCH_QUERY);
    logger
        .trace("Searching with '{}' for '{}' of classes {} with limit={}, offset={}.", searchQuery,
            keyword, classes, offset, limit);
    return this.<SelectQueryResult>query(searchQuery, true).value();
  }

  @Override
  public void setup() throws KGDAOException {

  }
}
