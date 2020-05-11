package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet.FacetFilter;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.resources.ResourceCollection;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.resources.ResourceList;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ClassResourceService;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.AllResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.exploration.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.sparqlbuilder.facet.FacetedSearchQueryBuilder;
import java.beans.Expression;
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
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.SparqlFunction;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Query;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger logger = LoggerFactory.getLogger(AllResources.class);

  public static final String OID = "esm.source.all";

  private static final int LOAD_LIMIT = 100000;

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

  /**
   * Filter for all resources that match with the given classes to include and to exclude. Both
   * arguments can be null or empty.
   *
   * @param include classes of which returned resources must be a member of (at least one).
   * @param exclude classes of which returned resources shall be no member.
   * @return the filtered resources.
   */
  private ResourceList filterInstancesFor(List<Resource> include, List<Resource> exclude) {
    Set<Resource> resourceSet;
    if (include != null && !include.isEmpty()) {
      resourceSet = Collections.emptySet();
      for (Resource includedClassResource : include) {
        Optional<Set<Resource>> instancesOfClassOpt = classResourceService
            .getInstancesOfClass(includedClassResource);
        if (instancesOfClassOpt.isPresent()) {
          resourceSet = Sets.union(resourceSet, instancesOfClassOpt.get());
        }
      }
    } else {
      resourceSet = new HashSet<>(allResourcesService.getResourceList());
    }
    if (exclude != null && !exclude.isEmpty()) {
      for (Resource excludedClassResource : exclude) {
        Optional<Set<Resource>> instancesOfClassOpt = classResourceService
            .getInstancesOfClass(excludedClassResource);
        if (instancesOfClassOpt.isPresent()) {
          resourceSet = Sets.difference(resourceSet, instancesOfClassOpt.get());
        }
      }
    }
    return new ResourceList(resourceSet);
  }

  /**
   * Filter for all resources that match with the given classes to include and to exclude as well as
   * with the given facets. This can only be solved with a query language. All three arguments can
   * be null or empty.
   *
   * @param include classes of which returned resources must be a member of (at least one).
   * @param exclude classes of which returned resources shall be no member.
   * @param facets a list of facets that must be matched by the resources.
   * @return the filtered resources.
   */
  private ResourceList queryFor(List<Resource> include, List<Resource> exclude,
      List<FacetFilter> facets) {
    /*
     *  SELECT DISTINCT ?s WHERE {
     *    { ?s ?p [] }
     *     UNION
     *    { [] ?p ?s }
     *    ${body}
     *    FILTER(isIRI(?s)) .
     *  }
     *  LIMIT ${limit}
     *  OFFSET ${offset}
     */
    // variables
    Variable resource = SparqlBuilder.var("s");
    Variable property = SparqlBuilder.var("p");
    //
    SelectQuery query = Queries.SELECT(resource).distinct()
        .where(GraphPatterns.union(resource.has(property, Rdf.bNode()),
            Rdf.bNode().has(property, resource))
            .filter(Expressions.function(SparqlFunction.IS_IRI, resource)));
    /* construct query body */
    FacetedSearchQueryBuilder facetBody = FacetedSearchQueryBuilder.forSubject("s");
    if (include != null && !include.isEmpty()) {
      facetBody.includeInstancesOfClasses(include.stream()
          .map(Resource::value).collect(Collectors.toList()));
    }
    if (exclude != null && !exclude.isEmpty()) {
      facetBody.excludeInstancesOfClassResources(exclude.stream()
          .map(Resource::value).collect(Collectors.toList()));
    }
    if (facets != null && !facets.isEmpty()) {
      facets.forEach(facetBody::addPropertyFacet);
    }
    query = query.where(facetBody.build()).limit(LOAD_LIMIT);
    /* perform query */
    List<Resource> resourceList = new LinkedList<>();
    List<Map<String, RDFTerm>> results;
    int offset = 0;
    do {
      Query currentQuery = query.offset(offset);
      String currentQueryString = currentQuery.getQueryString();
      logger.debug("Query constructed for '{}': {}", OID, currentQueryString);
      results = sparqlService.<SelectQueryResult>query(currentQueryString, true).value();
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

  @Override
  public ResourceCollection apply(AllResourcesPayload payload) {
    checkArgument(payload != null,
        "The payload for the \"esm.source.all\" operator must not be null.");
    boolean include =
        payload.getIncludedClasses() != null && !payload.getIncludedClasses().isEmpty();
    boolean exclude =
        payload.getExcludedClasses() != null && !payload.getExcludedClasses().isEmpty();
    boolean facets = payload.getFacetFilters() != null && !payload.getFacetFilters().isEmpty();
    if (!include && !exclude && !facets) { // if no filtering, then return all resources.
      return new ResourceList(allResourcesService.getResourceList());
    } else if (!facets) { // if only class filtering is required.
      return filterInstancesFor(payload.getIncludedClasses(), payload.getExcludedClasses());
    } else { // class and facet filtering.
      return queryFor(payload.getIncludedClasses(), payload.getExcludedClasses(),
          payload.getFacetFilters());
    }
  }
}
