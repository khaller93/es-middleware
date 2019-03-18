package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

  private final HTreeMap<Integer, Set<String>> classDbMap;

  @Autowired
  public ResourceClassWithSPARQLService(
      SPARQLService sparqlService,
      AllResourcesService allResourcesService, @Qualifier("persistent-mapdb") DB mapDB) {
    this.sparqlService = sparqlService;
    this.allResourcesService = allResourcesService;
    this.mapDB = mapDB;
    this.classDbMap = mapDB.hashMap(UID, SERIALIZER.INTEGER, SERIALIZER.JAVA).createOrOpen();
  }

  @Override
  public Optional<Set<Resource>> getClassesOf(Resource instance) {
    checkArgument(instance != null, "The given instance must not be null.");
    Optional<Integer> optResourceKey = allResourcesService.getResourceKey(instance);
    if (optResourceKey.isPresent()) {
      Set<String> classSet = classDbMap.get(optResourceKey.get());
      if (classSet != null) {
        return Optional.of(classSet.stream().map(Resource::new).collect(Collectors.toSet()));
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
              BlankOrIRIJsonUtil::stringForSPARQLResourceOf).collect(Collectors.joining("\n"))),
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
      Map<Integer, Set<String>> classDbIntermediateMap = new HashMap<>();
      for (Entry<Resource, Set<Resource>> entry : classResourceMap.entrySet()) {
        Optional<Integer> optResourceKey = allResourcesService.getResourceKey(entry.getKey());
        if (optResourceKey.isPresent()) {
          classDbIntermediateMap.put(optResourceKey.get(),
              entry.getValue().stream().map(Resource::getId).collect(Collectors.toSet()));
        } else {
          logger.warn("No key could be fetched for resource {}.", entry.getKey());
        }
      }
      classDbMap.putAll(classDbIntermediateMap);
    }
    mapDB.commit();
  }
}
