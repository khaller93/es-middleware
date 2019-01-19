package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.FTSDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.SparqlDAOStateChangeEvent;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphDAOException;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.facet.Facet;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOFailedStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOInitStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOReadyStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOUpdatingStatus;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.FacetedSearchQueryBuilder;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link KGFullTextSearchDAO} for GraphDB using the in-built lucene.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("GraphDBLucene")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GraphDbLucene implements KGFullTextSearchDAO {

  private static final Logger logger = LoggerFactory.getLogger(GraphDbLucene.class);

  private static final String FTS_QUERY = "PREFIX luc: <http://www.ontotext.com/owlim/lucene#>\n"
      + "SELECT ?resource ?score {\n"
      + "  ?resource <${name}> \"${keyword}\" ; \n"
      + "     luc:score ?score .\n"
      + "  ${body}\n"
      + "} ORDER BY DESC (?score)\n"
      + "${offset}\n"
      + "${limit}\n";

  private static final String INSERT_INDEX_DATA_QUERY =
      "PREFIX luc: <http://www.ontotext.com/owlim/lucene#>\n"
          + "INSERT DATA {\n"
          + "%s\n"
          + "<%s> luc:createIndex \"true\".\n"
          + "}";

  private static final String BATCH_UPDATE_QUERY =
      "PREFIX luc: <http://www.ontotext.com/owlim/lucene#>\n"
          + "INSERT DATA { <%s> luc:updateIndex _:b1 . }";

  private TaskExecutor taskExecutor;

  private ApplicationContext context;
  private KGSparqlDAO sparqlDAO;
  private GraphDbLuceneConfig graphDbLuceneConfig;

  private KGDAOStatus status;

  @Autowired
  public GraphDbLucene(ApplicationContext context, @Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      GraphDbLuceneConfig graphDbLuceneConfig, TaskExecutor taskExecutor) {
    this.context = context;
    this.sparqlDAO = sparqlDAO;
    this.graphDbLuceneConfig = graphDbLuceneConfig;
    this.taskExecutor = taskExecutor;
    this.status = new KGDAOInitStatus();
  }

  @PostConstruct
  public void setUp() {
    if (graphDbLuceneConfig.shouldBeInitialized()) {
      logger.debug("The GraphDb Lucene index will be initialized.");
      taskExecutor.execute(() -> {
        try (RepositoryConnection con = ((RDF4JSparqlDAO) sparqlDAO).getRepository()
            .getConnection()) {
          con.prepareUpdate(String.format(INSERT_INDEX_DATA_QUERY,
              graphDbLuceneConfig.getConfigTriples(), graphDbLuceneConfig.getLuceneIndexIRI()));
          setStatus(new KGDAOReadyStatus());
        } catch (Exception e) {
          setStatus(new KGDAOFailedStatus("Initializing the GraphDb Lucene index failed.", e));
          throw e;
        }
      });
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

  protected synchronized void setStatus(KGDAOStatus status, long eventId) {
    checkArgument(status != null, "The specified status must not be null.");
    if (!this.status.getCode().equals(status.getCode())) {
      KGDAOStatus prevStatus = this.status;
      this.status = status;
      context.publishEvent(new FTSDAOStateChangeEvent(this, eventId, status, prevStatus,
          Instant.now()));
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
    valueMap.put("name", graphDbLuceneConfig.getLuceneIndexIRI());
    valueMap.put("keyword", keyword);
    /* build facets */
    FacetedSearchQueryBuilder queryBuilder = FacetedSearchQueryBuilder.forSubject("resource");
    queryBuilder.includeInstancesOfClasses(classes);
    if (facets != null) {
      facets.forEach(queryBuilder::addPropertyFacet);
    }
    valueMap.put("body", queryBuilder.getQueryBody());
    /* windowing */
    valueMap.put("offset", offset != null ? "OFFSET " + offset.toString() : "");
    valueMap.put("limit", limit != null ? "LIMIT " + limit.toString() : "");
    String filledFtsQuery = new StringSubstitutor(valueMap)
        .replace(FTS_QUERY);
    logger.trace(
        "Query resulting from FTS call for {} with parameters (offset={}, limit={}, classes={}).",
        filledFtsQuery, offset, limit, classes);
    return sparqlDAO.<SelectQueryResult>query(filledFtsQuery, true).value();
  }

  @Override
  public KGDAOStatus getStatus() {
    return status;
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

  /**
   * Performs a batch update for the index. GraphDb provides the ability to update all non-indexed
   * resources.
   */
  private void performBatchUpdateOfIndex() {
    logger.debug("Batch updating the lucene index for '{}'.", graphDbLuceneConfig.getName());
    try (RepositoryConnection con = ((RDF4JSparqlDAO) sparqlDAO).getRepository()
        .getConnection()) {
      con.prepareUpdate(String.format(BATCH_UPDATE_QUERY, graphDbLuceneConfig.getLuceneIndexIRI()));
    } catch (Exception e) {
      logger.error("Error while updating the Lucene update. {}", e.getMessage());
      setStatus(new KGDAOFailedStatus("Updating the GraphDb Lucene index failed.", e));
      throw e;
    }
  }

  @EventListener
  public void handleSPARQLDAOStateChange(SparqlDAOStateChangeEvent event) {
    logger.info("The SPARQL DAO was updated, and lucene index will be updated now too.");
    setStatus(new KGDAOUpdatingStatus());
    taskExecutor.execute(() -> {
      try {
        performBatchUpdateOfIndex();
        setStatus(new KGDAOReadyStatus(), event.getEventId());
      } catch (Exception e) {
        setStatus(new KGDAOFailedStatus("Updating the lucene index failed.", e));
        throw e;
      }
    });
  }
}
