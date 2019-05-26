package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.common.knowledgegraph.SPARQLService;
import at.ac.tuwien.ifs.es.middleware.sparql.result.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.common.analysis.RegisterForAnalyticalProcessing;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.QueueLong.Node.SERIALIZER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This service maintains the classes an instance is member of.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = ResourceClassWithSPARQLService.UID, requiresSPARQL = true, prerequisites = {
    AllResourcesService.class})
public class ResourceClassWithSPARQLService implements ResourceClassService {

  private static final Logger logger = LoggerFactory
      .getLogger(ResourceClassWithSPARQLService.class);

  private static final int LOAD_SIZE = 10000;

  public static final String UID = "esm.service.analytics.dataset.resource.class.all";

  private static final String ALL_INSTANCE_CLASSES_QUERY =
      "SELECT DISTINCT ?resource ?class WHERE {\n"
          + "    VALUES ?resource {\n"
          + "        %s\n"
          + "    }\n"
          + "    ?resource a/rdfs:subClassOf* ?class .\n"
          + "    FILTER (isIRI(?class)) .\n"
          + "}\n";

  private final SPARQLService sparqlService;
  private final AllResourcesService allResourcesService;
  private final DB mapDB;

  private final HTreeMap<Integer, int[]> resourceClassDbMap;

  @Autowired
  public ResourceClassWithSPARQLService(
      SPARQLService sparqlService,
      AllResourcesService allResourcesService, @Qualifier("persistent-mapdb") DB mapDB) {
    this.sparqlService = sparqlService;
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.resourceClassDbMap = mapDB.hashMap(UID, SERIALIZER.INTEGER, SERIALIZER.INT_ARRAY).createOrOpen();
  }

  @Override
  public Optional<Set<Resource>> getClassesOf(Resource instance) {
    checkArgument(instance != null, "The given instance must not be null.");
    Optional<Integer> optResourceKey = allResourcesService.getResourceKey(instance);
    if (optResourceKey.isPresent()) {
      int[] classKeySet = resourceClassDbMap.get(optResourceKey.get());
      if (classKeySet != null) {
        Set<Resource> classResourceSet = new HashSet<>();
        for (int n = 0; n < classKeySet.length; n++) {
          int classKey = classKeySet[n];
          Optional<String> resourceIdOptional = allResourcesService.getResourceIdFor(classKey);
          if (resourceIdOptional.isPresent()) {
            classResourceSet.add(new Resource(resourceIdOptional.get()));
          }
        }
        return Optional.of(classResourceSet);
      }
    }
    return Optional.empty();
  }

  @Override
  public void compute() {
    List<Resource> resourceList = allResourcesService.getResourceList();
    int total = resourceList.size();
    int page = (int) Math.ceil(((double) total) / LOAD_SIZE);
    for (int n = 0; n < page; n++) {
      int start = n * LOAD_SIZE;
      int end = total > (start + LOAD_SIZE) ? (start + LOAD_SIZE) : total;
      /* prepare map */
      Map<Resource, Set<Resource>> classResourceMap = new HashMap<>();
      for (Resource resource : resourceList.subList(start, end)) {
        classResourceMap.put(resource, new HashSet<>());
      }
      /* fetch class relationships */
      List<Map<String, RDFTerm>> results = sparqlService.<SelectQueryResult>query(
          String.format(ALL_INSTANCE_CLASSES_QUERY, resourceList.subList(start, end).stream().map(
              RDFTermJsonUtil::stringForSPARQLResourceOf).collect(Collectors.joining("\n"))),
          true)
          .value();
      for (Map<String, RDFTerm> row : results) {
        Resource resource = new Resource((BlankNodeOrIRI) row.get("resource"));
        classResourceMap.get(resource).add(new Resource((BlankNodeOrIRI) row.get("class")));
      }
      logger.trace(
          "Loaded class relationships for {} resources. {} resources has already been loaded.",
          (end - start), end);
      /* store the class relationships */
      Map<Integer, int[]> classDbIntermediateMap = new HashMap<>();
      for (Entry<Resource, Set<Resource>> entry : classResourceMap.entrySet()) {
        Optional<Integer> optResourceKey = allResourcesService.getResourceKey(entry.getKey());
        if (optResourceKey.isPresent()) {
          classDbIntermediateMap.put(optResourceKey.get(),
              ArrayUtils
                  .toPrimitive(entry.getValue().stream().map(allResourcesService::getResourceKey)
                      .filter(Optional::isPresent).map(Optional::get).toArray(Integer[]::new)));
        } else {
          logger.warn("No key could be fetched for resource {}.", entry.getKey());
        }
      }
      resourceClassDbMap.putAll(classDbIntermediateMap);
    }
    mapDB.commit();
  }
}
