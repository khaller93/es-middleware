package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.fts.FullTextSearchDAOFailedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.fts.FullTextSearchDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.fts.FullTextSearchDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.fts.FullTextSearchDAOUpdatingEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JKnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOFailedStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOInitStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOReadyStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOUpdatingStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link KGFullTextSearchDAO} for GraphDB using the in-built lucene.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("InBuiltLucene")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GraphDbLucene implements KGFullTextSearchDAO {

  private static final Logger logger = LoggerFactory.getLogger(GraphDbLucene.class);

  private static final String FTS_QUERY = "PREFIX luc: <http://www.ontotext.com/owlim/lucene#>\n"
      + "SELECT ?resource ?score {\n"
      + "  ?resource <${name}> \"${keyword}\" ; \n"
      + "     luc:score ?score .\n"
      + "  ${class-filter}\n"
      + "} ORDER BY DESC (?score)\n"
      + "${offset}\n"
      + "${limit}\n";

  private final static String[] FTS_CLASSES_FILTER = new String[]{
      "\n?resource a %s .\n",
      "\n?resource a ?class .\nFILTER(?class in (%s)) .\n"
  };

  private static final String INSERT_INDEX_DATA_QUERY =
      "PREFIX luc: <http://www.ontotext.com/owlim/lucene#>\n"
          + "INSERT DATA {\n"
          + "%s\n"
          + "<%s> luc:createIndex \"true\".\n"
          + "}";

  private static final String BATCH_UPDATE_QUERY =
      "PREFIX luc: <http://www.ontotext.com/owlim/lucene#>\n"
          + "INSERT DATA { <%s> luc:updateIndex _:b1 . }";

  private ExecutorService threadPool = Executors.newCachedThreadPool();

  private ApplicationContext context;
  private KGSparqlDAO sparqlDAO;
  private GraphDbLuceneConfig graphDbLuceneConfig;

  private KGDAOStatus status;

  @Autowired
  public GraphDbLucene(ApplicationContext context, @Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      GraphDbLuceneConfig graphDbLuceneConfig) {
    this.context = context;
    this.sparqlDAO = sparqlDAO;
    this.graphDbLuceneConfig = graphDbLuceneConfig;
    this.status = new KGDAOInitStatus();
  }

  @PostConstruct
  public void setUp() {
    this.threadPool = Executors.newCachedThreadPool();
    if (graphDbLuceneConfig.shouldBeInitialized()) {
      logger.debug("The GraphDb Lucene index will be initialized.");
      threadPool.submit(() -> {
        try (RepositoryConnection con = ((RDF4JKnowledgeGraphDAO) sparqlDAO).getRepository()
            .getConnection()) {
          con.prepareUpdate(
              String.format(INSERT_INDEX_DATA_QUERY, graphDbLuceneConfig.getConfigTriples(),
                  graphDbLuceneConfig.getLuceneIndexIRI()));
          context.publishEvent(new FullTextSearchDAOReadyEvent(GraphDbLucene.this));
          this.status = new KGDAOReadyStatus();
        } catch (Exception e) {
          context.publishEvent(new FullTextSearchDAOFailedEvent(GraphDbLucene.this,
              "Initializing the GraphDb Lucene index failed.", e));
          this.status = new KGDAOFailedStatus("Initializing the GraphDb Lucene index failed.", e);
          throw e;
        }
      });
    } else {
      context.publishEvent(new FullTextSearchDAOReadyEvent(this));
      this.status = new KGDAOReadyStatus();
    }
  }

  @Override
  public List<Map<String, RDFTerm>> searchFullText(String keyword,
      List<BlankNodeOrIRI> classes, Integer offset, Integer limit) {
    logger
        .debug("FTS call for {} was triggered with parameters: offset={}, limit={}, and classes={}",
            keyword, offset, limit, classes);
    Map<String, String> valueMap = new HashMap<>();
    valueMap.put("name", graphDbLuceneConfig.getLuceneIndexIRI());
    valueMap.put("keyword", keyword);
    valueMap.put("class-filter", prepareFilter(classes));
    valueMap.put("offset", offset != null ? "OFFSET " + offset.toString() : "");
    valueMap.put("limit", limit != null ? "LIMIT " + limit.toString() : "");
    String filledFtsQuery = new StringSubstitutor(valueMap)
        .replace(FTS_QUERY);
    logger.trace(
        "Query resulting from FTS call for {} with parameters (offset={}, limit={}, classes={}).",
        filledFtsQuery, offset, limit, classes);
    return ((SelectQueryResult) sparqlDAO.query(filledFtsQuery, true)).value();
  }

  @Override
  public KGDAOStatus getFTSStatus() {
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
      return String.format(FTS_CLASSES_FILTER[0],
          BlankOrIRIJsonUtil.stringForSPARQLResourceOf(classes.get(0)));
    } else {
      return String.format(FTS_CLASSES_FILTER[1],
          classes.stream().map(BlankOrIRIJsonUtil::stringForSPARQLResourceOf)
              .collect(Collectors.joining(",")));
    }
  }

  /**
   * Performs a batch update for the index. GraphDb provides the ability to update all non-indexed
   * resources.
   */
  private void performBatchUpdateOfIndex() {
    logger.debug("Batch updating the lucene index for '{}'.", graphDbLuceneConfig.getName());
    try (RepositoryConnection con = ((RDF4JKnowledgeGraphDAO) sparqlDAO).getRepository()
        .getConnection()) {
      con.prepareUpdate(String.format(BATCH_UPDATE_QUERY, graphDbLuceneConfig.getLuceneIndexIRI()));
    } catch (Exception e) {
      logger.error("Error while updating the Lucene update. {}", e.getMessage());
      context.publishEvent(new FullTextSearchDAOFailedEvent(GraphDbLucene.this,
          "Initializing the GraphDb Lucene index failed.", e));
      this.status = new KGDAOFailedStatus("Updating the GraphDb Lucene index failed.", e);
      throw e;
    }
  }

  @EventListener
  public void handleSPARQLDAOUpdated(SPARQLDAOUpdatedEvent updatedEvent) {
    logger.info("The SPARQL DAO was updated, and lucene index will be updated now too.");
    context.publishEvent(new FullTextSearchDAOUpdatingEvent(this));
    this.status = new KGDAOUpdatingStatus();
    threadPool.submit(() -> {
      try {
        performBatchUpdateOfIndex();
        context.publishEvent(new FullTextSearchDAOUpdatedEvent(GraphDbLucene.this));
        this.status = new KGDAOReadyStatus();
      } catch (Exception e) {
        context.publishEvent(
            new FullTextSearchDAOFailedEvent(GraphDbLucene.this,
                "Updating the lucene index failed.", e));
        this.status = new KGDAOFailedStatus("Updating the lucene index failed.", e);
        throw e;
      }
    });
  }

  @PreDestroy
  public void tearDown() {
    if (threadPool != null) {
      threadPool.shutdown();
    }
  }
}
