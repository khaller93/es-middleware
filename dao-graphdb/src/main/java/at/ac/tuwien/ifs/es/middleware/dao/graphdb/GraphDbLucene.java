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
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.facet.FacetedSearchQueryBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.InsertDataQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
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

  private static final Prefix LUC = SparqlBuilder.prefix("luc",
      Rdf.iri("http://www.ontotext.com/owlim/lucene#"));

  private static final String INSERT_INDEX_DATA_QUERY =
      "PREFIX luc: <http://www.ontotext.com/owlim/lucene#>\n"
          + "INSERT DATA {\n"
          + "%s\n"
          + "<%s> luc:createIndex \"true\".\n"
          + "}";

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
    /*
     *  PREFIX luc: <http://www.ontotext.com/owlim/lucene#>
     *
     *  SELECT ?resource ?score {
     *    ?resource luc:${name} "${keyword}" ;
     *              luc:score ?score .
     *    ${FACETS}
     *  } ORDER BY DESC (?score)
     *  OFFSET ${offset}
     *  LIMIT ${LIMIT}
     */
    /* variables */
    Variable resource = SparqlBuilder.var("resource");
    Variable score = SparqlBuilder.var("score");
    /* construct main query */
    SelectQuery query = Queries.SELECT(resource, score).prefix(LUC)
        .where(resource.has(LUC.iri(graphDbLuceneConfig.getName()), Rdf.literalOf(keyword)),
            resource.has(LUC.iri("score"), score));
    /* build facets */
    if ((classes != null && classes.size() > 0) || (facets != null && facets.size() > 0)) {
      FacetedSearchQueryBuilder queryBuilder = FacetedSearchQueryBuilder.forSubject("resource");
      if (classes != null && classes.size() > 0) {
        queryBuilder.includeInstancesOfClasses(classes);
      }
      if (facets != null && facets.size() > 0) {
        facets.forEach(queryBuilder::addPropertyFacet);
      }
      query = query.where(queryBuilder.build());
    }
    query = query.orderBy(score.desc());
    /* adjust window */
    if (offset != null) {
      query = query.offset(offset);
    }
    if (limit != null) {
      query = query.limit(limit);
    }
    String filledFtsQuery = query.getQueryString();
    logger.trace(
        "Query resulting from FTS call for {} with parameters (offset={}, limit={}, classes={}).",
        filledFtsQuery, offset, limit, classes);
    return sparqlDAO.<SelectQueryResult>query(filledFtsQuery, true).value();
  }

  /**
   * Performs a batch update for the index. GraphDb provides the ability to update all non-indexed
   * resources.
   */
  private void performBatchUpdateOfIndex() {
    logger.debug("Batch updating the lucene index for '{}'.", graphDbLuceneConfig.getName());
    try (RepositoryConnection con = ((RDF4JSparqlDAO) sparqlDAO).getRepository()
        .getConnection()) {
      /*
       *  PREFIX luc: <http://www.ontotext.com/owlim/lucene#>
       *
       *  INSERT DATA { <%s> luc:updateIndex _:b1 . }
       */
      InsertDataQuery query = Queries.INSERT_DATA(
          LUC.iri(graphDbLuceneConfig.getName()).has(LUC.iri("updateIndex"),
              Rdf.bNode("b1"))).prefix(LUC);
      logger.trace("Batch Update Query: {}", query.getQueryString());
      con.prepareUpdate(query.getQueryString());
    } catch (Exception e) {
      logger.error("Error while updating the Lucene update. {}", e.getMessage());
      throw e;
    }
  }
}
