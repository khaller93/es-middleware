package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAOStatusChangeListener;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.FTSDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.LuceneIndexedRDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphDAOException;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSetupException;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.facet.Facet;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOFailedStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOInitStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOReadyStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.FacetedSearchQueryBuilder;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
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
  private ApplicationContext context;

  private KGDAOStatus status;
  private List<KGDAOStatusChangeListener> statusChangeListenerList = new LinkedList<>();

  @Autowired
  public RDF4JLuceneFullTextSearchDAO(@Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      ApplicationContext context) {
    this.sparqlDAO = sparqlDAO;
    this.context = context;
    this.status = new KGDAOInitStatus();
  }

  @PostConstruct
  public void setUp() {
    if (!(sparqlDAO instanceof LuceneIndexedRDF4JSparqlDAO)) {
      String message = String.format(
          "The given SPARQL DAO must be indexed with Lucene and implement the '%s' interface.",
          RDF4JLuceneFullTextSearchDAO.class.getName());
      RuntimeException exception = new KnowledgeGraphSetupException(message);
      setStatus(new KGDAOFailedStatus(message, exception));
      throw exception;
    } else {
      setStatus(new KGDAOReadyStatus());
    }
  }

  @Override
  public void addStatusChangeListener(KGDAOStatusChangeListener changeListener) {
    checkArgument(changeListener != null, "The given change listener must not be null.");
    statusChangeListenerList.add(changeListener);
  }

  protected synchronized void setStatus(KGDAOStatus status) {
    checkArgument(status != null, "The specified status must not be null.");
    if (!this.status.getCode().equals(status.getCode())) {
      KGDAOStatus prevStatus = this.status;
      this.status = status;
      context.publishEvent(new FTSDAOStateChangeEvent(this, status, prevStatus,
          Instant.now()));
      statusChangeListenerList.forEach(changeListener -> changeListener.onStatusChange(status));
    }
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit) {
    return searchFullText(keyword, classes, offset, limit, null);
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit, List<Facet> facets) throws KnowledgeGraphDAOException {
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

  @Override
  public KGDAOStatus getStatus() {
    return status;
  }
}
