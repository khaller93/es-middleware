package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.RP;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * This is an implementation get {@link LeastCommonSubSumersService} that uses the {@link
 * SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Service
@RegisterForAnalyticalProcessing(name = LCSWithSPARQLService.UID)
public class LCSWithSPARQLService implements LeastCommonSubSumersService {

  private static final Logger logger = LoggerFactory.getLogger(LeastCommonSubSumersService.class);

  public static final String UID = "esm.service.analytics.dataset.leastcommonsubsumers";

  private static final String LEAST_COMMON_SUBSUMER_QUERY =
      "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
          + "\n"
          + "select ?resource1 ?resource2 ?class where { \n"
          + "    ?resource1 (a/rdfs:subClassOf*) ?class.\n"
          + "    ?resource2 (a/rdfs:subClassOf*) ?class.\n"
          + "    FILTER NOT EXISTS {\n"
          + "        ?resource1 (a/rdfs:subClassOf*) ?subClass.\n"
          + "        ?resource2 (a/rdfs:subClassOf*) ?subClass.\n"
          + "        ?subClass rdfs:subClassOf+ ?class.\n"
          + "        FILTER (?class != ?subClass && isIRI(?subClass)).\n"
          + "    }\n"
          + "    FILTER(isIRI(?class)).\n"
          + "}";

  private final SPARQLService sparqlService;
  private final DB mapDb;

  private final HTreeMap<RP, Set<String>> subsumerMap;

  @Autowired
  public LCSWithSPARQLService(SPARQLService sparqlService, DB mapDb) {
    this.sparqlService = sparqlService;
    this.mapDb = mapDb;
    this.subsumerMap = mapDb.hashMap(UID, Serializer.JAVA, Serializer.JAVA).createOrOpen();
  }

  @Override
  public void compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to compute least common subsummers for all resource pairs.");
    List<Map<String, RDFTerm>> resultMap = ((SelectQueryResult) sparqlService
        .query(LEAST_COMMON_SUBSUMER_QUERY, true)).value();
    Map<ResourcePair, Set<Resource>> subsumerIntermediateMap = new HashMap<>();
    for (Map<String, RDFTerm> row : resultMap) {
      Resource left = new Resource((BlankNodeOrIRI) row.get("resource1"));
      Resource right = new Resource((BlankNodeOrIRI) row.get("resource2"));
      BiFunction<ResourcePair, Set<Resource>, Set<Resource>> comp = (pair, resources) ->
          resources != null ? resources : new HashSet<>();
      Resource clazz = new Resource((BlankNodeOrIRI) row.get("class"));
      subsumerIntermediateMap.compute(ResourcePair.of(left, right), comp).add(clazz);
    }
    subsumerMap.putAll(subsumerIntermediateMap.entrySet().stream().collect(Collectors
        .toMap(e -> RP.of(e.getKey()),
            e -> e.getValue().stream().map(Resource::getId).collect(Collectors.toSet()))));
    mapDb.commit();
    logger.info("Least common subsumers for all resource pairs issued on {} computed on {}.",
        issueTimestamp, Instant.now());
  }

  @Override
  public synchronized Set<Resource> getLeastCommonSubSumersFor(ResourcePair resourcePair) {
    Set<String> resourceSet = subsumerMap.get(RP.of(resourcePair));
    if (resourceSet != null) {
      return resourceSet.stream().map(Resource::new).collect(Collectors.toSet());
    } else {
      return Sets.newHashSet();
    }
  }

}
