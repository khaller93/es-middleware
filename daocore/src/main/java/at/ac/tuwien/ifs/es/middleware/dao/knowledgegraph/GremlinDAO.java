package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;

/**
 * This interface provides methods for traversing the knowledge graph using Gremlin. Not the whole
 * knowledge graph might be accessible with Gremlin, because at the moment not few triplestores
 * provide no Gremlin endpoint and synchronisation to a dedicated graph database must take place.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface GremlinDAO {

  /**
   * Returns the ability to traverse the knowledge graph.
   *
   * @return {@link GraphTraversalSource} for traversing the knowledge graph.
   */
  GraphTraversalSource traverse();

  /**
   * Gets the graph features of this Gremlin DAO.
   *
   * @return the graph features of this Gremlin DAO.
   */
  Features getFeatures();

}
