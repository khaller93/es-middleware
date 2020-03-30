package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.ldsd;

import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.AllResourcesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.GremlinService;
import ch.obermuhlner.math.big.BigDecimalMath;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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

  private final GremlinService gremlinService;
  private final AllResourcesService allResourcesService;
  private final PGS schema;
  private final DB mapDB;

  private final HTreeMap<int[], Long> cdMap;
  private final HTreeMap<int[], Long> ciiMap;
  private final HTreeMap<int[], Long> cioMap;

  @Autowired
  public WeightedLDSDAlphaWithGremlinService(GremlinService gremlinService,
      AllResourcesService allResourcesService, DB mapDB) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.allResourcesService = allResourcesService;
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

  private BigDecimal compute(GraphTraversal<Vertex, String> traversal, HTreeMap<int[], Long> map,
      Integer aKey) {
    BigDecimal value = BigDecimal.ZERO;
    gremlinService.lock();
    try {
      while (traversal.hasNext()) {
        String property = traversal.next();
        Optional<Integer> propertyKeyOpt = allResourcesService
            .getResourceKey(new Resource(property));
        if (propertyKeyOpt.isPresent()) {
          value = value.add(BigDecimal.ONE
              .divide(BigDecimal.ONE
                      .add(BigDecimalMath.log10(BigDecimal
                              .valueOf(map.get(new int[]{aKey, propertyKeyOpt.get()})),
                          MathContext.DECIMAL64)),
                  MathContext.DECIMAL64));
        }
      }
    } finally {
      gremlinService.unlock();
    }
    return value;
  }

  @Override
  public DecimalNormalizedAnalysisValue getValueFor(ResourcePair resourcePair) {
    // string IDs
    String aID = resourcePair.getFirst().getId();
    String bID = resourcePair.getSecond().getId();
    Optional<Integer> firstKeyOptional = allResourcesService
        .getResourceKey(resourcePair.getFirst());
    if (firstKeyOptional.isPresent()) {
      Optional<Integer> secondKeyOptional = allResourcesService
          .getResourceKey(resourcePair.getSecond());
      if (secondKeyOptional.isPresent()) {
        // integer IDs
        Integer aKey = firstKeyOptional.get();
        Integer bKey = secondKeyOptional.get();
        // cd(l_i, a, b)
        GraphTraversal<Vertex, String> cdTraversalAB = gremlinService.traversal().V()
            .has(schema.iri().identifierAsString(), aID).outE().as("e").inV()
            .has(schema.iri().identifierAsString(), bID).select("e").label();
        BigDecimal cdAB = compute(cdTraversalAB, cdMap, aKey);
        // cd(l_i, b, a)
        GraphTraversal<Vertex, String> cdTraversalBA = gremlinService.traversal().V()
            .has(schema.iri().identifierAsString(), bID).outE().as("e").inV()
            .has(schema.iri().identifierAsString(), aID).select("e").label();
        BigDecimal cdBA = compute(cdTraversalBA, cdMap, bKey);
        // cii(l_i. a, b)
        GraphTraversal<Vertex, String> ciiResult = gremlinService.traversal().V()
            .has(schema.iri().identifierAsString(), aID).inE().as("e1").outV().outE().as("e2").inV()
            .has(schema.iri().identifierAsString(), bID).
                select("e1").label().as("e1-l").select("e2").label().as("e2-l").where(P.eq("e1-l"));
        BigDecimal cii = compute(ciiResult, cdMap, aKey);
        // cio(l_i. a, b)
        GraphTraversal<Vertex, String> cioResult = gremlinService.traversal().V()
            .has(schema.iri().identifierAsString(), aID).outE().as("e1").inV().inE().as("e2").outV()
            .has(schema.iri().identifierAsString(), bID).
                select("e1").label().as("e1-l").select("e2").label().as("e2-l").where(P.eq("e1-l"));
        BigDecimal cio = compute(cioResult, cdMap, aKey);
        return new DecimalNormalizedAnalysisValue(
            BigDecimal.ONE
                .divide(BigDecimal.ONE.add(cdAB).add(cdBA).add(cii).add(cio),
                    MathContext.DECIMAL64));
      }
    }
    return null;
  }

  private <T> void processNorm(String name,
      GraphTraversal<T, Map<Vertex, Map<String, Long>>> traversal,
      HTreeMap<int[], Long> map) {
    Map<int[], Long> storageCache = new HashMap<>();
    int n = 0;
    if (traversal.hasNext()) {
      Map<Vertex, Map<String, Long>> cdResult = traversal.next();
      for (Entry<Vertex, Map<String, Long>> entry : cdResult.entrySet()) {
        Resource resource = new Resource(schema.iri().<String>apply(entry.getKey()));
        Optional<Integer> resourceKeyOptional = allResourcesService.getResourceKey(resource);
        if (resourceKeyOptional.isPresent()) {
          Map<String, Long> propertyMap = entry.getValue();
          for (Entry<String, Long> propertyMapEntry : propertyMap.entrySet()) {
            Resource property = new Resource(propertyMapEntry.getKey());
            Optional<Integer> propertyKeyOptional = allResourcesService.getResourceKey(property);
            if (propertyKeyOptional.isPresent()) {
              storageCache.put(new int[]{}, propertyMapEntry.getValue());
              n++;
              if (n % 10000 == 0) {
                logger.trace("Processed {} {} entries for LDSD.", n, name);
                map.putAll(storageCache);
                storageCache.clear();
              }
            }
          }
        }
      }
      if (!storageCache.isEmpty()) {
        map.putAll(storageCache);
      }
    }
    logger.debug("Finished to process all ({}) {} entries for LDSD.", n, name);
    logger.trace("{}: {}", name, map.entrySet());
    mapDB.commit();
  }

  @Override
  public void compute() {
    // computing Cd(l_i, r_a), i.e. the total number of outgoing relationships given a resource.
    logger.debug("Compute the CD map for LDSD.");
    gremlinService.lock();
    try {
      GraphTraversal<Edge, Map<Vertex, Map<String, Long>>> cdTraversal = gremlinService
          .traversal().E().<Vertex, Map<String, Long>>group()
          .by(__.outV())
          .by(__.groupCount().by(__.label()));
      processNorm("CD", cdTraversal, cdMap);
      // computing Cii(l_i, r_a)
      logger.debug("Compute the Cio map for LDSD.");
      GraphTraversal<Vertex, Map<Vertex, Map<String, Long>>> ciiTraversal = gremlinService
          .traversal().V().as("a").outE()
          .as("e1").inV().inE().as("e2").where(
              __.select("e1").label().as("e1-l").select("e2").label().as("e2-l")
                  .where(P.eq("e1-l"))).outV().<Vertex, Map<String, Long>>group().by(__.select("a"))
          .by(__.groupCount().by(__.select("e1").label()));
      processNorm("Cii", ciiTraversal, ciiMap);
      // computing Cio(l_i, r_a)
      GraphTraversal<Vertex, Map<Vertex, Map<String, Long>>> cioTraversal = gremlinService
          .traversal().V().as("a").inE()
          .as("e1").outV().outE().as("e2").where(
              __.select("e1").label().as("e1-l").select("e2").label().as("e2-l")
                  .where(P.eq("e1-l"))).inV().<Vertex, Map<String, Long>>group().by(__.select("a"))
          .by(__.groupCount().by(__.select("e1").label()));
      processNorm("Cio", cioTraversal, cioMap);
    } finally {
      gremlinService.unlock();
    }
  }
}
