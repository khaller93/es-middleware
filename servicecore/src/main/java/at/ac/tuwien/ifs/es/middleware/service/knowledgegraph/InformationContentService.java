package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * This service provides information content services for the maintained knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class InformationContentService {

  private GremlinService gremlinService;

  public InformationContentService(@Autowired GremlinService gremlinService) {
    this.gremlinService = gremlinService;
  }

  /**
   * Gets all the classes in the knowledge graph. Classes are those, which have a {@code rdf:type}
   * relationship with {@code rdfs:Class} and/or have an incoming {@code rdf:type} relationship.
   *
   * @return all the classes in the knowledge graph.
   */
  @Cacheable("gremlin")
  @SuppressWarnings("unchecked")
  public List<Resource> getAllClasses() {
    return gremlinService.traversal().V()
        .union(__.inE("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").inV(),
            __.as("c").out("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
                .V("http://www.w3.org/2000/01/rdf-schema#Class").select("c")).dedup().toList()
        .stream().map(v -> new Resource(BlankOrIRIJsonUtil.valueOf((String) v.id())))
        .collect(Collectors.toList());
  }

  /**
   * Gets a map of all classes in the knowledge graph with their information content. It takes the
   * classes resulting from {@link InformationContentService#getAllClasses()} and computes the
   * number of all unique members  as well as the number per class. The graph query also takes
   * {@code rdfs:subClassOf} relationships into consideration (class hierarchies).
   * <p/>
   * Then the information content is computed with the formula {@code -(#class_instances/total) *
   * log(#class_instances/total)} for each class. The result is then returned in a map.
   *
   * @return all the classes in the knowledge graph with their information content.
   */
  @Cacheable("gremlin")
  public Map<Resource, Double> getInformationContentForClasses() {
    GraphTraversalSource g = gremlinService.traversal();
    Long total = g.V(getAllClasses().stream().map(Resource::getId).toArray())
        .in("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").dedup().count().next();
    Map<Object, Object> classInstancesMap = g
        .V(getAllClasses().stream().map(Resource::getId).toArray()).until(
            __.or(__.not(__.in("http://www.w3.org/2000/01/rdf-schema#subClassOf")),
                __.cyclicPath()))
        .repeat(__.in("http://www.w3.org/2000/01/rdf-schema#subClassOf")).group().by(__.id())
        .by(__.in("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").dedup().count()).next();
    return classInstancesMap.entrySet().stream().collect(
        Collectors.toMap(e -> new Resource(BlankOrIRIJsonUtil.valueOf((String) e.getKey())),
            e -> {
              Double p = ((((Long) e.getValue()).doubleValue()) / total);
              return -p * Math.log(p);
            }));
  }

}
