package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation get {@link SameAsResourceService} using the {@link SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = "esm.service.analytics.dataset.sameas", requiresSPARQL = true,
    prerequisites = {AllResourcesService.class})
public class SameAsResourceWithSPARQLService implements SameAsResourceService {

  private static final Logger logger = LoggerFactory
      .getLogger(SameAsResourceWithSPARQLService.class);

  private static final Long LOAD_LIMIT = 100000L;

  private static final String DUPLICATES_QUERY =
      "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
          + "\n"
          + "SELECT ?s ?same WHERE { \n"
          + "\t?s owl:sameAs ?same .\n"
          + "    FILTER (?s != ?same) .\n"
          + "}\n"
          + "OFFSET %d\n"
          + "LIMIT %d";


  private final SPARQLService sparqlService;
  private final AllResourcesService allResourcesService;

  private final DB mapDB;

  private final HTreeMap<Integer, int[]> sameAsMap;

  @Autowired
  public SameAsResourceWithSPARQLService(SPARQLService sparqlService,
      AllResourcesService allResourcesService, @Qualifier("persistent-mapdb") DB mapDB) {
    this.sparqlService = sparqlService;
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.sameAsMap = mapDB
        .hashMap("esm.service.analytics.dataset.sameas", Serializer.INTEGER, Serializer.INT_ARRAY)
        .createOrOpen();
  }

  @Override
  public void compute() {
    logger.debug("Start to compute the 'owl:sameAs' mapping.");
    Map<Resource, Set<Resource>> sameAsIntermediateMap = new HashMap<>();
    int offset = 0;
    List<Map<String, RDFTerm>> results;
    do {
      results = sparqlService.<SelectQueryResult>query(
          String.format(DUPLICATES_QUERY, offset, LOAD_LIMIT), true).value();
      if (results != null) {
        for (Map<String, RDFTerm> row : results) {
          Resource keyResource = new Resource((BlankNodeOrIRI) row.get("s"));
          sameAsIntermediateMap.compute(keyResource, (resource, sameAsSet) ->
              sameAsSet != null ? sameAsSet : new HashSet<>())
              .add(new Resource((BlankNodeOrIRI) row.get("same")));
        }
        offset += results.size();
        logger.trace("Loaded {} sameAs relationships. {} sameAs relationships in total.",
            offset + results.size());
      } else {
        break;
      }
    } while (results.size() == LOAD_LIMIT);
    Map<Integer, int[]> sameAsMapIntermediate = new HashMap<>();
    sameAsIntermediateMap.forEach((key, value) -> {
      Optional<Integer> resourceKey = allResourcesService.getResourceKey(key);
      if (resourceKey.isPresent()) {
        int[] sameAsResources = ArrayUtils
            .toPrimitive(value.stream().map(allResourcesService::getResourceKey).filter(
                Optional::isPresent).map(Optional::get).toArray(Integer[]::new));
        if (sameAsResources.length > 0) {
          sameAsMapIntermediate.put(resourceKey.get(), sameAsResources);
        }
      }
    });
    sameAsMap.putAll(sameAsMapIntermediate);
    mapDB.commit();
  }

  @Override
  public Set<Resource> getSameAsResourcesFor(Resource resource) {
    checkArgument(resource != null,
        "The given resource for computing the same as resources must not be null.");
    Optional<Integer> resourceKey = allResourcesService.getResourceKey(resource);
    if (resourceKey.isPresent()) {
      int[] resourceKeys = sameAsMap.get(resourceKey.get());
      if (resourceKeys != null && resourceKeys.length > 0) {
        Set<Resource> resourceSet = new HashSet<>();
        for (int n = 0; n < resourceKeys.length; n++) {
          Optional<String> optResourceId = allResourcesService.getResourceIdFor(resourceKeys[n]);
          optResourceId.ifPresent(s -> resourceSet.add(new Resource(s)));
        }
        return resourceSet;
      }
    }
    return Sets.newHashSet();
  }

}
