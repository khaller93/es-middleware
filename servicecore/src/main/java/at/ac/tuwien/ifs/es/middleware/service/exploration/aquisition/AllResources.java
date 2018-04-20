package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition.AllResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.sparql.SPARQLService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionSource} that acquires all resources, potentially
 * only of specific classes or part of specific namespaces.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.source.all")
public class AllResources implements AcquisitionSource<AllResourcesPayload> {

  private static final Logger logger = LoggerFactory.getLogger(AllResources.class);

  private static final String ALL_QUERY = "SELECT DISTINCT ?s WHERE {\n?s ?p ?o .\n ${include}\n ${exclude}\n}";
  private static final String ALL_GRAPH_QUERY = "SELECT DISTINCT ?s WHERE {\nGRAPH ?g {?s ?p ?o .\n ${include}\n ${exclude}\n}\n${namespace}\n}";

  private static final String[] INCLUDE_FILTER_TEMPLATE = {
      "FILTER EXISTS {\n"
          + "?s a %s .\n"
          + "}",
      "FILTER EXISTS {\n"
          + "?s a ?class .\n"
          + "FILTER(?class in (%s)) .\n"
          + "}"
  };
  private static final String[] EXCLUDE_FILTER_TEMPLATE = {
      "FILTER NOT EXISTS {\n"
          + "?s a %s .\n"
          + "}",
      "FILTER NOT EXISTS {\n"
          + "?s a ?class .\n"
          + "FILTER(?class in (%s)) .\n"
          + "}"
  };
  private static final String[] NAMESPACES_FILTER_TEMPlATE = {
      "FILTER {?g = %s}",
      "FILTER NOT EXISTS {\n"
          + "FILTER(?g in (%s)) .\n"
          + "}"
  };

  private final SPARQLService sparqlService;

  @Autowired
  public AllResources(SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public Class<AllResourcesPayload> getParameterClass() {
    return AllResourcesPayload.class;
  }

  /**
   * Fills the filter templates for the "all" query.
   */
  private String prepareFilterBlock(String[] filterTemplate, List<Resource> includedClasses) {
    if (includedClasses == null || includedClasses.isEmpty()) {
      return "";
    } else if (includedClasses.size() == 1) {
      return String.format(filterTemplate[0],
          BlankOrIRIJsonUtil.stringForSPARQLResourceOf(includedClasses.get(0)));
    } else {
      return String.format(filterTemplate[1],
          includedClasses.stream().map(BlankOrIRIJsonUtil::stringForSPARQLResourceOf)
              .reduce((a, b) -> a + "," + b).orElse(""));
    }
  }

  @Override
  public ExplorationContext apply(AllResourcesPayload payload) {
    String allQueryTemplate = ALL_QUERY;
    Map<String, String> valuesMap = new HashMap<>();
    List<Resource> namespaces = payload.getNamespaces();
    if (namespaces != null && !namespaces.isEmpty()) {
      allQueryTemplate = ALL_GRAPH_QUERY;
      valuesMap.put("namespace",
          prepareFilterBlock(NAMESPACES_FILTER_TEMPlATE, payload.getNamespaces()));
    }
    valuesMap
        .put("include", prepareFilterBlock(INCLUDE_FILTER_TEMPLATE, payload.getIncludedClasses()));
    valuesMap
        .put("exclude", prepareFilterBlock(EXCLUDE_FILTER_TEMPLATE, payload.getExcludedClasses()));
    // Reads the result.
    String query = new StrSubstitutor(valuesMap).replace(allQueryTemplate);
    logger.debug("Executing the query '{}' for all resources operator.", query);
    SelectQueryResult result = (SelectQueryResult) sparqlService.query(query, true);
    return ResourceList.of(result, "s");
  }
}
