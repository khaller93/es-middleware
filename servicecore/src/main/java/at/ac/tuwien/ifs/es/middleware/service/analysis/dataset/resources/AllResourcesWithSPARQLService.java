package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.text.StrSubstitutor;
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
 * This class provide a concrete implmentation {@link AllResourcesService} that uses {@link
 * SPARQLService} for fetching all the resources and {@link at.ac.tuwien.ifs.es.middleware.service.analysis.MapDB}
 * to store all the resources for fast access.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@RegisterForAnalyticalProcessing(name = AllResourcesWithSPARQLService.UID, requiresSPARQL = true)
public class AllResourcesWithSPARQLService implements AllResourcesService {

  private static final Logger logger = LoggerFactory.getLogger(AllResourcesWithSPARQLService.class);

  public static final String UID = "esm.service.analytics.dataset.all.resources";

  private static final long LOAD_LIMIT = 100000L;

  private static final String ALL_RESOURCE_IRIS_QUERY = "SELECT DISTINCT ?resource WHERE {\n"
      + "    {?resource ?p1 []}\n"
      + "     UNION\n"
      + "    {\n"
      + "        [] ?p2 ?resource .\n"
      + "    } \n"
      + "    FILTER (isIRI(?resource)) .\n"
      + "}\n"
      + "OFFSET ${offset}\n"
      + "LIMIT ${limit}";

  private final SPARQLService sparqlService;
  private final DB mapDB;

  private final HTreeMap<String, Integer> resourceKeyMap;
  private final HTreeMap<Integer, String> resourceIdMap;

  @Autowired
  public AllResourcesWithSPARQLService(
      SPARQLService sparqlService,
      @Qualifier("persistent-mapdb") DB mapDB) {
    this.sparqlService = sparqlService;
    this.mapDB = mapDB;
    this.resourceKeyMap = mapDB
        .hashMap(AllResourcesWithSPARQLService.UID + ".id.key", Serializer.STRING,
            Serializer.INTEGER)
        .createOrOpen();
    this.resourceIdMap = mapDB
        .hashMap(AllResourcesWithSPARQLService.UID + ".key.id", Serializer.INTEGER,
            Serializer.STRING).createOrOpen();

  }

  @Override
  public List<Resource> getResourceList() {
    return resourceKeyMap.keySet().stream().map(Resource::new).collect(Collectors.toList());
  }

  @Override
  public Optional<Integer> getResourceKey(Resource resource) {
    checkArgument(resource != null, "The given resource must not be null.");
    Integer val = resourceKeyMap.get(resource.getId());
    return val != null ? Optional.of(val) : Optional.empty();
  }

  @Override
  public Optional<String> getResourceIdFor(Integer key) {
    checkArgument(key != null, "The given resource key must not be null.");
    String resourceId = resourceIdMap.get(key);
    if (resourceId != null) {
      return Optional.of(resourceId);
    }
    return Optional.empty();
  }

  @Override
  public void compute() {
    long offset = 0;
    List<Map<String, RDFTerm>> results;
    String resourceQuery = new StrSubstitutor(Collections.singletonMap("limit", LOAD_LIMIT))
        .replace(ALL_RESOURCE_IRIS_QUERY);
    do {
      results = sparqlService.<SelectQueryResult>query(
          new StrSubstitutor(Collections.singletonMap("offset", offset)).replace(resourceQuery),
          true).value();
      if (results != null) {
        for (Map<String, RDFTerm> row : results) {
          String resourceId = BlankOrIRIJsonUtil
              .stringValue((BlankNodeOrIRI) row.get("resource"));
          if (!resourceKeyMap.containsKey(resourceId)) {
            int key = resourceKeyMap.size() + 1;
            resourceKeyMap.put(resourceId, key);
            resourceIdMap.put(key, resourceId);
          }
        }
        offset += results.size();
        logger.trace("Loaded {} resources. {} resources already loaded.", results.size(), offset);
      } else {
        break;
      }
    } while (results.size() == LOAD_LIMIT);
    mapDB.commit();
  }


}
