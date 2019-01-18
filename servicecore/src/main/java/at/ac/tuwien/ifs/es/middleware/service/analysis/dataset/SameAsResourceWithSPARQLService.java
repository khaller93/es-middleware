package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisPipelineProcessor;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
@AnalyticalProcessing(name = "esm.service.analytics.dataset.sameas")
public class SameAsResourceWithSPARQLService implements SameAsResourceService {

  private static final Logger logger = LoggerFactory
      .getLogger(SameAsResourceWithSPARQLService.class);

  private static final Long LOAD_LIMIT = 10000L;

  private static final String DUPLICATES_QUERY =
      "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
          + "\n"
          + "SELECT ?s ?same WHERE { \n"
          + "\t?s owl:sameAs ?same .\n"
          + "    FILTER (?s != ?same) .\n"
          + "}\n"
          + "OFFSET %d\n"
          + "LIMIT %d";


  private SPARQLService sparqlService;
  private Cache sameAsCache;
  private AnalysisPipelineProcessor processor;

  @Autowired
  public SameAsResourceWithSPARQLService(SPARQLService sparqlService,
      AnalysisPipelineProcessor processor,
      CacheManager cacheManager) {
    this.sparqlService = sparqlService;
    this.processor = processor;
    this.sameAsCache = cacheManager.getCache("sameas");
  }

  @PostConstruct
  private void setUp() {
    //processor.registerAnalysisService(this, true, false, false, null);
  }

  @Override
  public void compute() {
    Instant startedTime = Instant.now();
    logger.debug("Start to compute the 'owl:sameAs' mapping.");
    Map<Resource, Set<Resource>> sameAsMap = new HashMap<>();
    int offset = 0;
    List<Map<String, RDFTerm>> results;
    do {
      results = sparqlService.<SelectQueryResult>query(
          String.format(DUPLICATES_QUERY, offset, LOAD_LIMIT), true).value();
      if (results != null) {
        for (Map<String, RDFTerm> row : results) {
          Resource keyResource = new Resource((BlankNodeOrIRI) row.get("s"));
          sameAsMap.compute(keyResource, (resource, sameAsSet) ->
              sameAsSet != null ? sameAsSet : new HashSet<>())
              .add(new Resource((BlankNodeOrIRI) row.get("same")));
        }
        offset += results.size();
        logger.info("Loaded {} sameAs relationships.", offset + results.size());
      } else {
        break;
      }
    } while (results.size() == LOAD_LIMIT);
    if (sameAsCache != null) {
      for (Map.Entry<Resource, Set<Resource>> entry : sameAsMap.entrySet()) {
        sameAsCache.put(entry.getKey(), entry.getValue());
      }
    }
    logger.debug("'owl:sameAs' mapping issued at {} computed on {}.", startedTime, Instant.now());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<Resource> getSameAsResourcesFor(Resource resource) {
    Set<Resource> sameAsSet = (Set<Resource>) sameAsCache.get(resource, Set.class);
    if (sameAsSet != null) {
      return sameAsSet;
    } else {
      return Sets.newHashSet();
    }
  }

}
