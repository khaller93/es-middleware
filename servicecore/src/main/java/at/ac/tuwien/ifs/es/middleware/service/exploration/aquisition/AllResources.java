package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.payload.acquisition.AllResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.FacetedSearchQueryBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation get {@link AcquisitionSource} that acquires all resources, potentially
 * only get specific classes or part get specific namespaces.
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

  private static final int LOAD_LIMIT = 100000;

  private static final String ALL_QUERY = "SELECT DISTINCT ?s WHERE { \n"
      + "    { ?s ?p [] . FILTER(isIRI(?s)) .}\n"
      + "     UNION\n"
      + "    { [] ?p ?s . FILTER(isIRI(?s)) .}\n"
      + "    ${body}\n"
      + "}\n"
      + "LIMIT ${limit}\n"
      + "OFFSET ${offset}\n";

  private static final String ALL_GRAPH_QUERY = "SELECT DISTINCT ?s WHERE {\nGRAPH ?g {?s ?p ?o .\n ${body}\n}\n${namespace}\n}";

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

  private String prepareNamespaceFilterBlock(List<Resource> includedClasses) {
    if (includedClasses == null || includedClasses.isEmpty()) {
      return "";
    } else if (includedClasses.size() == 1) {
      return String.format(NAMESPACES_FILTER_TEMPlATE[0],
          BlankOrIRIJsonUtil.stringForSPARQLResourceOf(includedClasses.get(0)));
    } else {
      return String.format(NAMESPACES_FILTER_TEMPlATE[1],
          includedClasses.stream().map(BlankOrIRIJsonUtil::stringForSPARQLResourceOf)
              .collect(Collectors.joining(",")));
    }
  }

  @Override
  public ExplorationContext apply(AllResourcesPayload payload) {
    String allQueryTemplate = ALL_QUERY;
    Map<String, String> valuesMap = new HashMap<>();
    /* prepare the namespace filter */
    final List<Resource> namespaces = payload.getNamespaces();
    if (namespaces != null && !namespaces.isEmpty()) {
      allQueryTemplate = ALL_GRAPH_QUERY;
      valuesMap.put("namespace", prepareNamespaceFilterBlock(payload.getNamespaces()));
    }
    /* construct query body */
    FacetedSearchQueryBuilder queryBuilder = FacetedSearchQueryBuilder.forSubject("s");
    /* include classes pattern */
    queryBuilder.includeInstancesOfClassResources(
        payload.getIncludedClasses() != null ? payload.getIncludedClasses()
            : Collections.emptyList());
    /* exclude classes pattern */
    queryBuilder.excludeInstancesOfClassResources(
        payload.getExcludedClasses() != null ? payload.getExcludedClasses()
            : Collections.emptyList());
    /* property facets */
    if (payload.getFacets() != null) {
      payload.getFacets().forEach(queryBuilder::addPropertyFacet);
    }
    valuesMap.put("body", queryBuilder.getQueryBody());
    valuesMap.put("limit", String.valueOf(LOAD_LIMIT));
    /* perform query */
    String allQuery = new StringSubstitutor(valuesMap).replace(allQueryTemplate);
    List<Resource> resourceList = new LinkedList<>();
    List<Map<String, RDFTerm>> results;
    int offset = 0;
    do {
      results = sparqlService.<SelectQueryResult>query(new StringSubstitutor(
              Collections.singletonMap("offset", offset)).replace(allQuery)
          , true).value();
      if (results != null) {
        for (Map<String, RDFTerm> row : results) {
          resourceList.add(new Resource((BlankNodeOrIRI) row.get("s")));
        }
        offset += results.size();
      } else {
        break;
      }
    } while (results.size() == LOAD_LIMIT);
    return new ResourceList(resourceList);
  }
}
