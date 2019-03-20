package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
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
      + "    { ?s ?p [] }\n"
      + "     UNION\n"
      + "    { [] ?p ?s }\n"
      + "    ${body}\n"
      + "    FILTER(isIRI(?s)) .\n"
      + "}\n"
      + "LIMIT ${limit}\n"
      + "OFFSET ${offset}\n";

  private final SPARQLService sparqlService;
  private final AllResourcesService allResourcesService;

  @Autowired
  public AllResources(SPARQLService sparqlService, AllResourcesService allResourcesService) {
    this.sparqlService = sparqlService;
    this.allResourcesService = allResourcesService;
  }

  @Override
  public Class<AllResourcesPayload> getParameterClass() {
    return AllResourcesPayload.class;
  }

  @Override
  public ExplorationContext apply(AllResourcesPayload payload) {
    checkArgument(payload != null,
        "The payload for the \"esm.source.all\" operator must not be null.");
    if ((payload.getIncludedClasses() == null || payload.getIncludedClasses().isEmpty()) &&
        (payload.getExcludedClasses() == null || payload.getExcludedClasses().isEmpty()) &&
        (payload.getFacets() == null || payload.getFacets().isEmpty())) {
      return new ResourceList(allResourcesService.getResourceList());
    } else if (payload.getFacets() == null || payload.getFacets().isEmpty()) {

    }
    Map<String, String> valuesMap = new HashMap<>();
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
    String allQuery = new StringSubstitutor(valuesMap).replace(ALL_QUERY);
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
