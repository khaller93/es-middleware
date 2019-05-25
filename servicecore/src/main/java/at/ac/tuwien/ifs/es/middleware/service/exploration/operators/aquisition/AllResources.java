package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources.ResourceCollection;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources.ResourceList;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ClassResourceService;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.AllResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.sparql.result.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.common.exploration.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.FacetedSearchQueryBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.collections.impl.factory.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionSource} that acquires all resources, potentially
 * only of specific classes.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow(AllResources.OID)
public class AllResources implements AcquisitionSource<ResourceCollection, AllResourcesPayload> {

  public static final String OID = "esm.source.all";

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
  private final ClassResourceService classResourceService;

  @Autowired
  public AllResources(SPARQLService sparqlService, AllResourcesService allResourcesService,
      ClassResourceService classResourceService) {
    this.sparqlService = sparqlService;
    this.allResourcesService = allResourcesService;
    this.classResourceService = classResourceService;
  }

  @Override
  public String getUID() {
    return "esm.source.all";
  }

  @Override
  public Class<ResourceCollection> getExplorationContextOutputClass() {
    return ResourceCollection.class;
  }

  @Override
  public Class<AllResourcesPayload> getPayloadClass() {
    return AllResourcesPayload.class;
  }

  @Override
  public ResourceCollection apply(AllResourcesPayload payload) {
    checkArgument(payload != null,
        "The payload for the \"esm.source.all\" operator must not be null.");
    if ((payload.getIncludedClasses() == null || payload.getIncludedClasses().isEmpty()) &&
        (payload.getExcludedClasses() == null || payload.getExcludedClasses().isEmpty()) &&
        (payload.getFacetFilters() == null || payload.getFacetFilters().isEmpty())) {
      return new ResourceList(allResourcesService.getResourceList());
    } else if (payload.getFacetFilters() == null || payload.getFacetFilters().isEmpty()) {
      Set<Resource> resourceSet;
      if (payload.getIncludedClasses() != null && !payload.getIncludedClasses().isEmpty()) {
        resourceSet = Collections.emptySet();
        for (Resource includedClassResource : payload.getIncludedClasses()) {
          Optional<Set<Resource>> instancesOfClassOpt = classResourceService
              .getInstancesOfClass(includedClassResource);
          if (instancesOfClassOpt.isPresent()) {
            resourceSet = Sets.union(resourceSet, instancesOfClassOpt.get());
          }
        }
      } else {
        resourceSet = new HashSet<>(allResourcesService.getResourceList());
      }
      if (payload.getExcludedClasses() != null && !payload.getExcludedClasses().isEmpty()) {
        for (Resource excludedClassResource : payload.getExcludedClasses()) {
          Optional<Set<Resource>> instancesOfClassOpt = classResourceService
              .getInstancesOfClass(excludedClassResource);
          if (instancesOfClassOpt.isPresent()) {
            resourceSet = Sets.difference(resourceSet, instancesOfClassOpt.get());
          }
        }
      }
      return new ResourceList(resourceSet);
    } else {
      Map<String, String> valuesMap = new HashMap<>();
      /* construct query body */
      FacetedSearchQueryBuilder queryBuilder = FacetedSearchQueryBuilder.forSubject("s");
      /* include classes pattern */
      queryBuilder.includeInstancesOfClassResources(
          payload.getIncludedClasses() != null ? payload.getIncludedClasses().stream()
              .map(Resource::value).collect(Collectors.toList())
              : Collections.emptyList());
      /* exclude classes pattern */
      queryBuilder.excludeInstancesOfClassResources(
          payload.getExcludedClasses() != null ? payload.getExcludedClasses().stream()
              .map(Resource::value).collect(Collectors.toList())
              : Collections.emptyList());
      /* property facets */
      if (payload.getFacetFilters() != null) {
        payload.getFacetFilters().forEach(queryBuilder::addPropertyFacet);
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
}
