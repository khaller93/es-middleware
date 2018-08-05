package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOReadyEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisEventStatus;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link SameAsResourceService} using the {@link SPARQLService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Service
@AnalyticalProcessing(name = "esm.service.analytics.dataset.sameas", online = true)
public class SameAsResourceWithSPARQLService implements SameAsResourceService {

  private static final Logger logger = LoggerFactory
      .getLogger(SameAsResourceWithSPARQLService.class);

  private static final String DUPLICATES_QUERY =
      "SELECT ?s (GROUP_CONCAT(str(?z); separator=\" >|< \") AS ?sameAs) WHERE {\n"
          + "  ?s owl:sameAs ?z.\n"
          + "  FILTER(?s != ?z). \n"
          + "} GROUP BY ?s";

  private SPARQLService sparqlService;
  private ApplicationEventPublisher eventPublisher;
  private TaskExecutor taskExecutor;

  private long lastUpdateTimestamp = 0L;
  private final Lock computationLock = new ReentrantLock();

  @Autowired
  public SameAsResourceWithSPARQLService(SPARQLService sparqlService,
      ApplicationEventPublisher eventPublisher, TaskExecutor taskExecutor) {
    this.sparqlService = sparqlService;
    this.eventPublisher = eventPublisher;
    this.taskExecutor = taskExecutor;
  }

//  @EventListener
  public void onApplicationEvent(SPARQLDAOReadyEvent event) {
    logger.debug("Recognized an SPARQL ready event {}.", event);
    startComputation(event.getTimestamp());
  }

//  @EventListener
  public void onApplicationEvent(SPARQLDAOUpdatedEvent event) {
    logger.debug("Recognized an SPARQL update event {}.", event);
    startComputation(event.getTimestamp());
  }

  private void startComputation(long eventTimestamp) {
    computationLock.lock();
    try {
      if (lastUpdateTimestamp < eventTimestamp) {
        taskExecutor.execute(this::compute);
        lastUpdateTimestamp = eventTimestamp;
      }
    } finally {
      computationLock.unlock();
    }
  }

  @Cacheable("sparql")
  @Override
  public Map<Resource, Set<Resource>> compute() {
    Instant startedTime = Instant.now();
    logger.debug("Start to compute the 'owl:sameAs' entities to all entities.");
    Map<Resource, Set<Resource>> sameAsMap = new HashMap<>();
    SelectQueryResult queryResult = (SelectQueryResult) sparqlService.query(DUPLICATES_QUERY, true);
    for (Map<String, RDFTerm> row : queryResult.value()) {
      Resource keyResource = new Resource((BlankNodeOrIRI) row.get("s"));
      String sameAsResourceString = ((Literal) row.get("sameAs")).getLexicalForm();
      sameAsMap.compute(keyResource, (resource, resources) -> {
        Set<Resource> sameAsResources = resources != null ? resources : new HashSet<>();
        Stream.of(sameAsResourceString.split(" >\\|< "))
            .map(s -> new Resource(BlankOrIRIJsonUtil.valueOf(s))).forEach(sameAsResources::add);
        return sameAsResources;
      });
    }
    logger.trace("SameAs map issued at {} computed on {}.", startedTime, Instant.now());
    return sameAsMap;
  }

  @Override
  public AnalysisEventStatus getStatus() {
    return null;
  }

  @Override
  public Set<Resource> getSameAsResourcesFor(Resource resource) {
    return compute().getOrDefault(resource, Sets.newHashSet());
  }
}
