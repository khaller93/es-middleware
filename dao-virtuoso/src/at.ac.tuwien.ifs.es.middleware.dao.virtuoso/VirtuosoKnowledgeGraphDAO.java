package at.ac.tuwien.ifs.es.middleware.dao.virtuoso;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOSetupException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOFailedStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOInitStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOReadyStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JSparqlDAO;
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
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link KnowledgeGraphDAOConfig} and {@link KGFullTextSearchDAO} for
 * Stardog using RDF4J.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://www.stardog.com/docs/">Stardog</a>
 * @since 1.0
 */
@Lazy
@Component("RemoteVirtuoso")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class VirtuosoKnowledgeGraphDAO extends RDF4JSparqlDAO implements
    KGFullTextSearchDAO {

  private static final Logger logger = LoggerFactory.getLogger(VirtuosoKnowledgeGraphDAO.class);

  private static final String SEARCH_QUERY = "SELECT DISTINCT ?resource (?sc AS ?score)\n"
      + " WHERE {\n"
      + " ?resource ?p ?l.\n"
      + " ?l bif:contains ${keyword} OPTION (score ?sc) .\n"
      + " ${body}\n"
      + "}\n"
      + "ORDER BY DESC(?sc)"
      + "${offset}\n"
      + "${limit}";

  /**
   * Creates a new Virtuoso DAO for {@link KnowledgeGraphDAOConfig} and {@link
   * KGFullTextSearchDAO}.
   *
   * @param virtuosoConfig that specifies properties for Stardog.
   */
  @Autowired
  public VirtuosoKnowledgeGraphDAO(VirtuosoConfig virtuosoConfig) throws KGDAOSetupException {
    this.init(new SPARQLRepository(virtuosoConfig.getAddress()));
  }

  @Override
  public void setup() throws KGDAOException {

  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit) {
    return searchFullText(keyword, classes, offset, limit, null);
  }

  /**
   * {@inheritDoc}
   *
   * @see <a href="http://docs.openlinksw.com/virtuoso/rdfsparqlrulefulltext/">Virtuoso FTS</a>
   */
  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit, List<FacetFilter> facets) throws KGDAOException {
    logger.debug("Searching for '{}' of classes {} with limit={}, offset={}.", keyword, classes,
        offset, limit);

    Map<String, String> queryValuesMap = new HashMap<>();
    queryValuesMap.put("keyword", Rdf.literalOf(keyword).getQueryString());
    /* windowing */
    queryValuesMap.put("offset", offset != null ? String.format("OFFSET %d", offset) : "");
    queryValuesMap.put("limit", limit != null ? String.format("LIMIT %d", limit) : "");
    /* build facets */
    if ((classes != null && classes.size() > 0) || (facets != null && facets.size() > 0)) {
      FacetedSearchQueryBuilder queryBuilder = FacetedSearchQueryBuilder.forSubject("resource");
      if (classes != null && classes.size() > 0) {
        queryBuilder.includeInstancesOfClasses(classes);
      }
      if (facets != null && facets.size() > 0) {
        facets.forEach(queryBuilder::addPropertyFacet);
      }
      queryValuesMap.put("body", queryBuilder.getQueryBody());
    } else {
      queryValuesMap.put("body", "");
    }
    String searchQuery = new StringSubstitutor(queryValuesMap).replace(SEARCH_QUERY);
    logger
        .trace("Searching with '{}' for '{}' of classes {} with limit={}, offset={}.",
            searchQuery, keyword, classes, offset, limit);
    return this.<SelectQueryResult>query(searchQuery, true).value();
  }
}
