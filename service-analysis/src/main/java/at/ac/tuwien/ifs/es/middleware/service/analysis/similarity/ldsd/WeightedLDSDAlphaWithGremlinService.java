package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.serializer.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SPARQLService;
import ch.obermuhlner.math.big.BigDecimalMath;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Primary
@Service
@RegisterForAnalyticalProcessing(name = WeightedLDSDAlphaWithGremlinService.WEIGHTED_LDSD_SIMILARITY_UID,
    requiresSPARQL = true, requiresGremlin = true,
    prerequisites = {AllResourcesService.class})
public class WeightedLDSDAlphaWithGremlinService implements
    LinkedDataSemanticDistanceMetricService {

  private static final Logger logger = LoggerFactory
      .getLogger(WeightedLDSDAlphaWithGremlinService.class);


  public static final String WEIGHTED_LDSD_SIMILARITY_UID = "esm.service.analytics.similarity.ldsd.alpha";

  private final SPARQLService sparqlService;
  private final AllResourcesService allResourcesService;
  private final TaskExecutor taskExecutor;
  private final DB mapDB;

  private final HTreeMap<int[], Long> cdMap;
  private final HTreeMap<int[], Long> ciiMap;
  private final HTreeMap<int[], Long> cioMap;

  @Autowired
  public WeightedLDSDAlphaWithGremlinService(SPARQLService sparqlService,
      AllResourcesService allResourcesService, TaskExecutor taskExecutor, DB mapDB) {
    this.sparqlService = sparqlService;
    this.allResourcesService = allResourcesService;
    this.taskExecutor = taskExecutor;
    this.mapDB = mapDB;
    this.cdMap = mapDB.hashMap(WEIGHTED_LDSD_SIMILARITY_UID + ".cd")
        .keySerializer(Serializer.INT_ARRAY)
        .valueSerializer(Serializer.LONG).createOrOpen();
    this.ciiMap = mapDB.hashMap(WEIGHTED_LDSD_SIMILARITY_UID + ".cii")
        .keySerializer(Serializer.INT_ARRAY)
        .valueSerializer(Serializer.LONG).createOrOpen();
    this.cioMap = mapDB.hashMap(WEIGHTED_LDSD_SIMILARITY_UID + ".cio")
        .keySerializer(Serializer.INT_ARRAY)
        .valueSerializer(Serializer.LONG).createOrOpen();
  }

  private class ComputeWithSPARQLCallback implements Callable<BigDecimal> {

    private final String query;
    private final HTreeMap<int[], Long> map;
    private final Integer aKey;

    ComputeWithSPARQLCallback(String query, HTreeMap<int[], Long> map, Integer aKey) {
      checkNotNull(query);
      checkNotNull(map);
      checkNotNull(aKey);
      checkArgument(!query.isEmpty(), "The given query must not be empty.");
      this.query = query;
      this.map = map;
      this.aKey = aKey;
    }

    @Override
    public BigDecimal call() throws Exception {
      BigDecimal value = BigDecimal.ZERO;
      for (Map<String, RDFTerm> row : sparqlService.<SelectQueryResult>query(query, true).value()) {
        Resource property = new Resource((IRI) row.get("p"));
        Optional<Integer> propertyKeyOpt = allResourcesService.getResourceKey(property);
        if (propertyKeyOpt.isPresent()) {
          Long total = map.get(new int[]{aKey, propertyKeyOpt.get()});
          if (total != null) {
            value = value.add(BigDecimal.ONE
                .divide(BigDecimal.ONE
                        .add(BigDecimalMath.log10(BigDecimal.valueOf(total), MathContext.DECIMAL64)),
                    MathContext.DECIMAL64));
          } else {
            logger.trace("Resource: {}, Property: {} not in the key map.", aKey, property.getId());
          }
        }
      }
      return value;
    }
  }

  @Override
  public DecimalNormalizedAnalysisValue getValueFor(ResourcePair resourcePair) {
    Optional<Integer> firstKeyOptional = allResourcesService
        .getResourceKey(resourcePair.getFirst());
    if (firstKeyOptional.isPresent()) {
      Optional<Integer> secondKeyOptional = allResourcesService
          .getResourceKey(resourcePair.getSecond());
      if (secondKeyOptional.isPresent()) {
        // integer IDs
        Integer aKey = firstKeyOptional.get();
        Integer bKey = secondKeyOptional.get();
        // sparql entries
        String aSparql = RDFTermJsonUtil.stringForSPARQLResourceOf(resourcePair.getFirst().value());
        String bSparql = RDFTermJsonUtil
            .stringForSPARQLResourceOf(resourcePair.getSecond().value());
        CompletionService<BigDecimal> completionService = new ExecutorCompletionService<>(
            taskExecutor);
        // cd(l_i, a, b)
        completionService.submit(new ComputeWithSPARQLCallback(String.format("select ?p where { \n"
            + "   %s ?p %s .\n"
            + "}", aSparql, bSparql), cdMap, aKey));
        // cd(l_i, b, a)
        completionService.submit(new ComputeWithSPARQLCallback(String.format("select ?p where { \n"
            + "   %s ?p %s .\n"
            + "}", bSparql, aSparql), cdMap, aKey));
        // cii(l_i. a, b)
        completionService.submit(new ComputeWithSPARQLCallback(String.format("SELECT ?p WHERE { \n"
            + "  ?rn ?p %s .\n"
            + "  ?rn ?p %s .\n"
            + "}", aSparql, bSparql), ciiMap, aKey));
        // cio(l_i. a, b)
        completionService.submit(new ComputeWithSPARQLCallback(String.format("SELECT ?p WHERE { \n"
            + "  %s ?p ?rn .\n"
            + "  %s ?p ?rn .\n"
            + "}", aSparql, bSparql), cioMap, aKey));
        // value
        BigDecimal sum = BigDecimal.ONE;
        for (int i = 0; i < 4; i++) {
          try {
            sum = sum.add(completionService.take().get());
          } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
          }
        }
        BigDecimal value = BigDecimal.ONE.divide(sum, MathContext.DECIMAL64);
        return new DecimalNormalizedAnalysisValue(value, value, null);
      }
    }
    return null;
  }

  private class ProcessWithSPARQLCallback implements Callable<Void> {

    private final String name;
    private final String query;
    private final HTreeMap<int[], Long> map;

    ProcessWithSPARQLCallback(String name, String query, HTreeMap<int[], Long> map) {
      checkNotNull(name);
      checkNotNull(query);
      checkNotNull(map);
      checkArgument(!name.isEmpty(), "The name must not be empty.");
      checkArgument(!query.isEmpty(), "The query must not be empty.");
      this.name = name;
      this.query = query;
      this.map = map;
    }

    @Override
    public Void call() throws Exception {
      logger.debug("Compute the {} map for LDSD.", name);
      Map<int[], Long> storageCache = new HashMap<>();
      int n = 0;
      for (Map<String, RDFTerm> row : sparqlService.<SelectQueryResult>query(query, true).value()) {
        Resource resource = new Resource((BlankNodeOrIRI) row.get("s"));
        Optional<Integer> resourceKeyOptional = allResourcesService.getResourceKey(resource);
        if (resourceKeyOptional.isPresent()) {
          Resource property = new Resource((IRI) row.get("p"));
          Optional<Integer> propertyKeyOptional = allResourcesService.getResourceKey(property);
          if (propertyKeyOptional.isPresent()) {
            storageCache
                .put(new int[]{resourceKeyOptional.get(), propertyKeyOptional.get()},
                    Long.parseLong(((Literal) row.get("cnt")).getLexicalForm()));
            n++;
            if (n % 10000 == 0) {
              logger.trace("Processed {} {} entries for LDSD.", n, name);
              map.putAll(storageCache);
              storageCache.clear();
            }
          }
        }
      }
      if (!storageCache.isEmpty()) {
        map.putAll(storageCache);
      }
      logger.trace("Processed {} {} entries for LDSD.", n, name);
      mapDB.commit();
      return null;
    }
  }

  @Override
  public void compute() {
    CompletionService<Void> completionService = new ExecutorCompletionService<>(taskExecutor);
    // computing Cd(l_i, r_a), i.e. the total number of outgoing relationships given a resource.
    completionService
        .submit(new ProcessWithSPARQLCallback("Cd",
            "SELECT ?s ?p (count(*) as ?cnt) WHERE { \n"
                + "    ?s ?p ?o .\n"
                + "} GROUP BY ?s ?p", cdMap));
    // computing Cii(l_i, r_a)
    completionService
        .submit(new ProcessWithSPARQLCallback("Cii",
            "SELECT ?s ?p (count(*) as ?cnt) WHERE { \n"
                + "    ?rn ?p ?s .\n"
                + "    ?rn ?p ?o .\n"
                + "    FILTER(isIRI(?s) && isIRI(?o)) ."
                + "} GROUP BY ?s ?p", ciiMap));
    // computing Cio(l_i, r_a)
    completionService
        .submit(new ProcessWithSPARQLCallback("Cio",
            "SELECT ?s ?p (count(*) as ?cnt) WHERE { \n"
                + "    ?s ?p ?rn .\n"
                + "    ?o ?p ?rn .\n"
                + "} GROUP BY ?s ?p", cioMap));
    // wait for tasks to finish
    for (int i = 0; i < 3; i++) {
      try {
        completionService.take();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
