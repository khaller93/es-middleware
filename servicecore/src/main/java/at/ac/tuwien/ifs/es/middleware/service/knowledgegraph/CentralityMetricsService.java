package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * This class implements methods for computing common centrality metrics.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@org.springframework.context.annotation.Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CentralityMetricsService {

  private static final Logger logger = LoggerFactory.getLogger(CentralityMetricsService.class);

  private GremlinService gremlinService;

  @Autowired
  public CentralityMetricsService(GremlinService gremlinService) {
    this.gremlinService = gremlinService;
  }

  /**
   * Gets the page rank of all resources in the maintained knowledge graph.
   *
   * @return the page rank of all resources in the maintained knowledge graph.
   */
  public Map<Resource, Double> getPageRank() {
    return getPageRank(Collections.emptyList());
  }

  /**
   * Gets the page rank of all member of the given {@code classes}. If empty, then all resources are
   * considered.
   *
   * @return the page rank of all members of the given {@code classes}.
   */
  @Cacheable("gremlin")
  public Map<Resource, Double> getPageRank(List<Resource> classes) {
    logger.debug("Computes page rank metric for {}.", classes.isEmpty() ? "all" : classes);
    GraphTraversal<Vertex, Vertex> g;
    if (!classes.isEmpty()) {
      g = gremlinService.traversal().withComputer().V().as("c")
          .out("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").until(__.or(
              __.hasId(classes.stream().map(Resource::getId).toArray()),
              __.cyclicPath()))
          .repeat(__.out("http://www.w3.org/2000/01/rdf-schema#subClassOf")).select("c");
    } else {
      g = gremlinService.traversal().withComputer().V().pageRank();
    }
    Map<Object, Object> pageRankMap = g.pageRank().group().by(__.id()).by(
        __.values("gremlin.pageRankVertexProgram.pageRank")).next();
    Map<Resource, Double> returnMap = new HashMap<>();
    for (Map.Entry<Object, Object> entry : pageRankMap.entrySet()) {
      returnMap.put(new Resource(BlankOrIRIJsonUtil.valueOf((String) entry.getKey())),
          (Double) entry.getValue());
    }
    return returnMap;
  }

  /**
   * Computes the degree centrality for all members of the given {@code classes}. If {@code classes}
   * is empty, all resources of the knowledge graph will be considered.
   *
   * @param classes of which the resources shall be a member.
   * @return the degree centrality for all members of the given {@code classes}, or of all
   * resources.
   */
  @Cacheable("gremlin")
  public Map<Resource, Long> getDegreeCentrality(List<Resource> classes) {
    logger.debug("Computes degree centrality metric for {}.", classes.isEmpty() ? "all" : classes);
    GraphTraversal<Vertex, Vertex> g;
    if (!classes.isEmpty()) {
      g = gremlinService.traversal().withComputer().V().as("c")
          .out("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").until(__.or(
              __.hasId(classes.stream().map(Resource::getId).toArray()),
              __.cyclicPath()))
          .repeat(__.out("http://www.w3.org/2000/01/rdf-schema#subClassOf")).select("c");
    } else {
      g = gremlinService.traversal().withComputer().V().pageRank();
    }
    GraphTraversal<Vertex, Map<String, Object>> resourceDegreesTraversal = g.project("v", "degree")
        .by(__.id()).by(__.bothE().count());
    Map<Resource, Long> degreeCentralityMap = new HashMap<>();
    while (resourceDegreesTraversal.hasNext()) {
      Map<String, Object> node = resourceDegreesTraversal.next();
      degreeCentralityMap
          .put(new Resource(BlankOrIRIJsonUtil.valueOf((String) node.get("v"))),
              (Long) node.get("degree"));
    }
    return degreeCentralityMap;
  }

  /**
   * Computes the degree centrality for all members of the given {@code classes}. If {@code classes}
   * is empty, all resources of the knowledge graph will be considered.
   *
   * @param classes of which the resources shall be a member.
   * @return the betweeness centrality for all members of the given {@code classes}, or of all
   * resources.
   */
  @SuppressWarnings("unchecked")
  @Cacheable("gremlin")
  public Map<Resource, Long> getBetweenessCentrality(List<Resource> classes) {
    logger.debug("Computes betweeness centrality metric for {}.", classes.isEmpty() ? "all" : classes);
    GraphTraversal<Vertex, Vertex> g;
    if (!classes.isEmpty()) {
      g = gremlinService.traversal().withComputer().V().as("c")
          .out("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").until(__.or(
              __.hasId(classes.stream().map(Resource::getId).toArray()),
              __.cyclicPath()))
          .repeat(__.out("http://www.w3.org/2000/01/rdf-schema#subClassOf")).select("c");
    } else {
      g = gremlinService.traversal().withComputer().V().pageRank();
    }
    Map<Object, Long> betweenessMap = g.as("v")
        .as("v").
            repeat(__.both().simplePath().as("v")).emit().
            filter(__.project("x", "y", "z").by(__.select(Pop.first, "v")).
                by(__.select(Pop.last, "v")).
                by(__.select(Pop.all, "v").count(Scope.local)).as("triple").
                coalesce(__.select("x", "y").as("a").
                        select("triples").unfold().as("t").
                        select("x", "y").where(P.eq("a")).
                        select("t"),
                    __.store("triples")).
                select("z").as("length").
                select("triple").select("z").where(P.eq("length"))). //6\
        select(Pop.all, "v").unfold().
            groupCount().next();
    //TODO: implement, consumes a lot of memory.
    return Collections.emptyMap();
  }

}
