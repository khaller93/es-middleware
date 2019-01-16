package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.FTSDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.SparqlDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.LuceneIndexedRDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSetupException;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOFailedStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOInitStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOReadyStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
          + "  ${class-filter}\n"
          + "} ORDER BY DESC(?score)\n"
          + "${offset}\n"
          + "${limit}\n";

  private KGDAOStatus status;
  private KGSparqlDAO sparqlDAO;
  private ApplicationContext context;

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

  protected synchronized void setStatus(KGDAOStatus status) {
    checkArgument(status != null, "The specified status must not be null.");
    if (!this.status.getCode().equals(status.getCode())) {
      KGDAOStatus prevStatus = this.status;
      this.status = status;
      context.publishEvent(new FTSDAOStateChangeEvent(this, status, prevStatus,
          Instant.now()));
    }
  }

  /**
   * Prepares a filter for the given list of class IRIs. This filter ensures that all returned
   * resources of the full-text-search belong to at least one of the given classes.
   *
   * @param classes of which a returned resource must be a member (at least of one given class).
   * @return a class filter for the full-text-search query.
   */
  private static String prepareFilter(List<BlankNodeOrIRI> classes) {
    if (classes == null || classes.isEmpty()) {
      return "";
    } else if (classes.size() == 1) {
      return String.format("?resource a/rdfs:subClassOf* %s .",
          BlankOrIRIJsonUtil.stringForSPARQLResourceOf(classes.get(0)));
    } else {
      return classes.stream().map(clazz -> String.format("{?resource a/rdfs:subClassOf* %s}",
          BlankOrIRIJsonUtil.stringForSPARQLResourceOf(clazz)))
          .collect(Collectors.joining("\nUNION\n"));
    }
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword,
      List<BlankNodeOrIRI> classes, Integer offset, Integer limit) {
    logger
        .debug("FTS call for {} was triggered with parameters: offset={}, limit={}, and classes={}",
            keyword, offset, limit, classes);
    Map<String, String> valueMap = new HashMap<>();
    valueMap.put("keyword", keyword);
    valueMap.put("class-filter", prepareFilter(classes));
    valueMap.put("offset", offset != null ? "OFFSET " + offset.toString() : "");
    valueMap.put("limit", limit != null ? "LIMIT " + limit.toString() : "");
    String filledFtsQuery = new StringSubstitutor(valueMap).replace(FTS_QUERY);
    return sparqlDAO.<SelectQueryResult>query(filledFtsQuery, true).value();
  }

  @Override
  public KGDAOStatus getStatus() {
    return status;
  }
}
