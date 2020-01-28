package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DependsOn;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOConnectionException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOSetupException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLExecutionException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet.FacetFilter;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.serializer.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.FacetedSearchQueryBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
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
@DependsOn(sparql = true)
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

  private final TaskExecutor taskExecutor;

  private final KGSparqlDAO sparqlDAO;
  private final GraphDbLuceneConfig graphDbLuceneConfig;

  @Autowired
  public GraphDbLucene(@Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      GraphDbLuceneConfig graphDbLuceneConfig, TaskExecutor taskExecutor) {
    this.sparqlDAO = sparqlDAO;
    this.graphDbLuceneConfig = graphDbLuceneConfig;
    this.taskExecutor = taskExecutor;
  }

  @Override
  public void setup() throws KGDAOSetupException, KGDAOConnectionException {
    if (graphDbLuceneConfig.shouldBeInitialized()) {
      logger.debug("The GraphDb Lucene index will be initialized.");
      try (RepositoryConnection con = ((RDF4JSparqlDAO) sparqlDAO).getRepository()
          .getConnection()) {
        con.prepareUpdate(String.format(INSERT_INDEX_DATA_QUERY,
            graphDbLuceneConfig.getConfigTriples(), graphDbLuceneConfig.getLuceneIndexIRI()));
      } catch (KGSPARQLExecutionException e) {
        throw new KGDAOConnectionException(e);
      } catch (Exception e) {
        throw new KGDAOSetupException(e);
      }
    }
    this.searchFullText("test", Collections.emptyList(), 0, 5);
  }

  @Override
  public void update(long timestamp) throws KGDAOException {
    logger.info("The SPARQL DAO was updated, and lucene index will be updated now too.");
    taskExecutor.execute(() -> {
      try {
        performBatchUpdateOfIndex();
      } catch (Exception e) {
        throw e;
      }
    });
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
    valueMap.put("name", graphDbLuceneConfig.getLuceneIndexIRI());
    valueMap.put("keyword", keyword.replace("\"", "\\\""));
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
          RDFTermJsonUtil.stringForSPARQLResourceOf(classes.get(0)));
    } else {
      return classes.stream().map(clazz -> String.format("{?resource a/rdfs:subClassOf* %s}",
          RDFTermJsonUtil.stringForSPARQLResourceOf(clazz)))
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
      throw e;
    }
  }
}
