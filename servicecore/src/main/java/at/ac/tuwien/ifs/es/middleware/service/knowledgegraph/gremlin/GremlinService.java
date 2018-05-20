package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin;

import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

/**
 * This service provides the ability to use {@link GraphTraversalSource} and {@link GraphComputer}
 * on the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface GremlinService {

  /**
   * Gets a {@link GraphTraversalSource} for the maintained knowledge graph.
   *
   * @return a {@link GraphTraversalSource} for the maintained knowledge graph.
   */
  GraphTraversalSource traversal();

}
