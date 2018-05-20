package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Service;

/**
 * This class implements methods for computing common similarity metrics.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@org.springframework.context.annotation.Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SimilarityMetricsService {

  private static final Logger logger = LoggerFactory.getLogger(SimilarityMetricsService.class);

  private GremlinService gremlinService;

  public SimilarityMetricsService(@Autowired GremlinService gremlinService) {
    this.gremlinService = gremlinService;
  }

  /**
   * Computes the distance between the first resource and second resource of a resource pair. If
   * first and second pair are the same resource, the distance will be {@code 0}.
   *
   * @param resourcePairs for which the distance shall be computed.
   * @return {@link Map} of resource pairs with the distance between given pair.
   */
  public Map<ResourcePair, Integer> getDistance(List<ResourcePair> resourcePairs) {
    logger.debug("Computing the distance for the resource pairs {}.", resourcePairs);
    Map<ResourcePair, Integer> distanceMap = new HashMap<>();
    for (ResourcePair pair : resourcePairs) {
      logger.info("Went into {}", pair);
      GraphTraversal<Vertex, Path> distance = gremlinService.traversal().V(pair.getFirst().getId())
          .until(__.hasId(pair.getSecond().getId())).repeat(__.both().dedup().simplePath()).path()
          .limit(1);
      //TODO: Detect non-path.
      if (distance.hasNext()) {
        logger.info("Computation finished for {}", pair);
        distanceMap.put(pair, distance.next().size() - 1);
      }
    }
    return distanceMap;
  }

}
