package at.ac.tuwien.ifs.es.middleware.dao.graphdb.lucene;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DependsOn;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOConnectionException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOSetupException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql.KGSPARQLExecutionException;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet.FacetFilter;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.facet.FacetedSearchQueryBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
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

  private enum CONNECTOR_STATE {MISSING, INITIALIZING, BUILT}

  private final KGSparqlDAO sparqlDAO;
  private final ConnectorConfig connectorConfig;
  private final ObjectMapper objectMapper;

  @Autowired
  public Lucene(@Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      ConnectorConfig connectorConfig, ObjectMapper objectMapper) {
    this.sparqlDAO = sparqlDAO;
    this.connectorConfig = connectorConfig;
    this.objectMapper = objectMapper;
  }

  @Override
  public void setup() throws KGDAOSetupException, KGDAOConnectionException {
    CONNECTOR_STATE status = getConnectorStatus();
    logger.debug("Fetched state '{}' from the Lucene connector in GraphDB.", status);
    if (status.equals(CONNECTOR_STATE.MISSING)) {
      try {
        this.createConnector(this.objectMapper.writeValueAsString(connectorConfig));
      } catch (KGSPARQLExecutionException e) {
        throw new KGDAOConnectionException(e);
      } catch (Exception e) {
        throw new KGDAOSetupException(e);
      }
      status = getConnectorStatus();
    }
    try {
      while (status.equals(CONNECTOR_STATE.INITIALIZING)) {
        logger.info("The graphDB Lucene index gets initialized.");
        Thread.sleep(1000);
        status = getConnectorStatus();
      }
    } catch (InterruptedException e) {
    }
    if (status.equals(CONNECTOR_STATE.BUILT)) {
      logger.info("The GraphDb Lucene index '{}' is ready.", connectorConfig.getName());
    }
  }

  /**
   * Gets the {@link CONNECTOR_STATE} of the Lucene connector. If the GraphDB instance returns an
   * empty list to a status query, or, if the status object cannot be serialized in a proper Json
   * object, then the state {@code MISSING} is returned.
   *
   * @return {@link CONNECTOR_STATE} of the Lucene connector.
   */
  protected CONNECTOR_STATE getConnectorStatus() {
    Variable cntStatus = SparqlBuilder.var("cntStatus");
    List<Map<String, RDFTerm>> results = this.sparqlDAO.<SelectQueryResult>query(
        Queries.SELECT(cntStatus)
            .where(PREFIX_CON_INST.iri(connectorConfig.getName())
                .has(PREFIX_CON.iri("connectorStatus"), cntStatus))
            .prefix(PREFIX_CON)
            .prefix(PREFIX_CON_INST).getQueryString(), false).value();
    if (results.isEmpty()) {
      return CONNECTOR_STATE.MISSING;
    }
    Literal status = (Literal) results.get(0).get("cntStatus");
    try {
      JsonNode statusNode = objectMapper.readTree(status.getLexicalForm());
      String statusCode = statusNode.get("status").asText();
      switch (statusCode) {
        case "BUILT":
          return CONNECTOR_STATE.BUILT;
        case "BUILDING":
          return CONNECTOR_STATE.INITIALIZING;
        default:
          break;
      }
    } catch (JsonProcessingException e) {
      logger.error("Couldn't fetch the status of the Lucene connector for GraphDB: {}",
          e.getMessage());
    }
    return CONNECTOR_STATE.MISSING;
  }

  /**
   * Issues a request to GraphDB to create a Lucene index with the give {@code configJson}. The
   * name of the index will correspond to {@link ConnectorConfig#getName()}.
   *
   * @throws KGSPARQLException if the SPARQL request to create the connector fails.
   */
  protected void createConnector(String configJson) throws KGSPARQLException {
    this.sparqlDAO.update(Queries.INSERT_DATA(PREFIX_CON_INST.iri(connectorConfig.getName())
            .has(PREFIX_CON.iri("createConnector"), Rdf.literalOf(configJson)))
        .prefix(PREFIX_CON)
        .prefix(PREFIX_CON_INST).getQueryString());
  }

  /**
   * Issues a request to GraphDB to drop the Lucene connector with the name specified by
   * {@link ConnectorConfig#getName()}.
   *
   * @throws KGSPARQLException if the SPARQL request to drop the connector fails.
   */
  protected void dropConnector() throws KGSPARQLException {
    this.sparqlDAO.update(Queries.INSERT_DATA(PREFIX_CON_INST.iri(connectorConfig.getName())
            .has(PREFIX_CON.iri("dropConnector"), Rdf.bNode()))
        .prefix(PREFIX_CON)
        .prefix(PREFIX_CON_INST).getQueryString());
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
                .isA(PREFIX_CON_INST.iri(connectorConfig.getName()))
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
