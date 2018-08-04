package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link LeastCommonSubSummersService} that uses the {@link
 * SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = "esm.service.analytics.dataset.leastcommonsubsummers", online = true)
public class LeastCommonSubSummersWithSPARQLService implements LeastCommonSubSummersService {

  private static final Logger logger = LoggerFactory.getLogger(LeastCommonSubSummersService.class);

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
          + "        FILTER (?class != ?subClass) .\n"
          + "    }\n"
          + "}";

  private SPARQLService sparqlService;

  @Autowired
  public LeastCommonSubSummersWithSPARQLService(SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Cacheable("sparql")
  public synchronized Map<ResourcePair, Set<Resource>> compute() {
    Instant issueTimestamp = Instant.now();
    logger.info("Starting to compute least common subsummers for all resource pairs.");
    List<Map<String, RDFTerm>> resultMap = ((SelectQueryResult) sparqlService
        .query(LEAST_COMMON_SUBSUMER_QUERY, true)).value();
    Map<ResourcePair, Set<Resource>> subsummerMap = new HashMap<>();
    for (Map<String, RDFTerm> row : resultMap) {
      Resource left = new Resource((BlankNodeOrIRI) row.get("resource1"));
      Resource right = new Resource((BlankNodeOrIRI) row.get("resource2"));
      BiFunction<ResourcePair, Set<Resource>, Set<Resource>> comp = (pair, resources) ->
          resources != null ? resources : new HashSet<>();
      Resource clazz = new Resource((BlankNodeOrIRI) row.get("class"));
      subsummerMap.compute(ResourcePair.of(left, right), comp).add(clazz);
    }
    logger.info("Least common subsummers for all resource pairs issued on {} computed on {}.",
        issueTimestamp, Instant.now());
    return subsummerMap;
  }

  @Override
  public synchronized Set<Resource> getLeastCommonSubSummersFor(ResourcePair resourcePair) {
    return compute().getOrDefault(resourcePair, Collections.emptySet());
  }

  @Override
  public AnalysisEventStatus getStatus() {
    return null;
  }

}
