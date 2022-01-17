package at.ac.tuwien.ifs.es.middleware.dao.graphdb.lucene;

import at.ac.tuwien.ifs.es.middleware.dao.graphdb.lucene.legacy.LegacyLuceneConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DependsOn;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOConnectionException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOSetupException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLExecutionException;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet.FacetFilter;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.facet.FacetedSearchQueryBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link KGFullTextSearchDAO} for GraphDB using the in-built Lucene
 * connector.
 *
 * @author Kevin Haller
 * @version 1.2
 * @since 1.2
 */
@Lazy
@Component("GraphDBLucene")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@DependsOn(sparql = true)
public class Lucene implements KGFullTextSearchDAO {

  private static final Logger logger = LoggerFactory.getLogger(Lucene.class);

  private static final Prefix PREFIX_CON = SparqlBuilder.prefix("con",
      Rdf.iri("http://www.ontotext.com/connectors/lucene#"));
  private static final Prefix PREFIX_CON_INST = SparqlBuilder.prefix("con-inst",
      Rdf.iri("http://www.ontotext.com/connectors/lucene/instance#"));

  private final TaskExecutor taskExecutor;

  private final KGSparqlDAO sparqlDAO;
  private final boolean shouldBeInitialized;
  private final LuceneConfig graphDbLuceneConfig;

  @Autowired
  public Lucene(@Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      @Value("${graphdb.lucene.initialize:#{false}}") boolean shouldBeInitialized,
      LuceneConfig graphDbLuceneConfig, TaskExecutor taskExecutor) {
    this.sparqlDAO = sparqlDAO;
    this.shouldBeInitialized = shouldBeInitialized;
    this.graphDbLuceneConfig = graphDbLuceneConfig;
    this.taskExecutor = taskExecutor;
  }

  @Override
  public void setup() throws KGDAOSetupException, KGDAOConnectionException {
    if (this.shouldBeInitialized) {
      logger.debug("The GraphDb Lucene index will be initialized.");
      try (RepositoryConnection con = ((RDF4JSparqlDAO) sparqlDAO).getRepository()
          .getConnection()) {
        //TODO: create an index, if it hasn't already been done.
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
    logger.info("The SPARQL DAO was updated, and lucene index will be synced automatically.");
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
     *  PREFIX luc: <http://www.ontotext.com/connectors/lucene#>
     *
     *  SELECT ?resource ?score {
     *    [] a con-inst:${name};
     *       con:query "${keyword}" ;
     *       con:entities ?resource .
     *    ?resource con:score ?score .
     *    ${FACETS}
     *  } ORDER BY DESC (?score)
     *  OFFSET ${offset}
     *  LIMIT ${LIMIT}
     */
    /* variables */
    Variable resource = SparqlBuilder.var("resource");
    Variable score = SparqlBuilder.var("score");
    /* construct main query */
    SelectQuery query = Queries.SELECT(resource, score).prefix(PREFIX_CON).prefix(PREFIX_CON_INST)
        .where(Rdf.bNode()
                .isA(PREFIX_CON_INST.iri("esm"))
                .andHas(PREFIX_CON.iri("query"), Rdf.literalOf(keyword))
                .andHas(PREFIX_CON.iri("entities"), resource),
            resource.has(PREFIX_CON.iri("score"), score));
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
}
