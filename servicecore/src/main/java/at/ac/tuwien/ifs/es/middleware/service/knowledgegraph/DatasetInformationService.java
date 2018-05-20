package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * This service computes and maintains information about the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DatasetInformationService {

  private static final Logger logger = LoggerFactory.getLogger(DatasetInformationService.class);

  private static final String DUPLICATES_QUERY =
      "SELECT ?s (GROUP_CONCAT(str(?z); separator=\" >|< \") AS ?sameAs) WHERE {\n"
          + "  ?s owl:sameAs ?z.\n"
          + "  FILTER(?s != ?z). \n"
          + "} GROUP BY ?s";

  private SPARQLService sparqlService;

  /**
   * Creates a new {@link DatasetInformationService} that implements common queries for the
   * knowledge graph.
   *
   * @param sparqlService that shall be used to query the knowledge graph.
   */
  public DatasetInformationService(@Autowired SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  /**
   * Prepares the sameAs entities for all entities and returns the map.
   *
   * @return a map that contains a list of sameAs entities for the key entity.
   */
  @Cacheable("sparql")
  public Map<Resource, List<Resource>> getSameAsEntitiesMap() {
    logger.debug("Start to compute the 'owl:sameAs' entities to all entities.");
    Map<Resource, List<Resource>> sameAsMap = new HashMap<>();
    SelectQueryResult queryResult = (SelectQueryResult) sparqlService.query(DUPLICATES_QUERY, true);
    for (Map<String, RDFTerm> row : queryResult.value()) {
      Resource keyResource = new Resource((BlankNodeOrIRI) row.get("s"));
      String sameAsResourceString = ((Literal) row.get("sameAs")).getLexicalForm();
      sameAsMap.compute(keyResource, (resource, resources) -> {
        List<Resource> sameAsResources = resources != null ? resources : new LinkedList<>();
        Stream.of(sameAsResourceString.split(" >\\|< "))
            .map(s -> new Resource(BlankOrIRIJsonUtil.valueOf(s))).forEach(sameAsResources::add);
        return sameAsResources;
      });
    }
    logger.trace("Computed the sameAs map {}.", sameAsMap.entrySet());
    return sameAsMap;
  }

}
