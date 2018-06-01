package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.*;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.InMemoryGremlinDAO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;

/**
 * This interface provides methods for traversing the knowledge graph using Gremlin.
 * <p/>
 * Instances of this DAO go through a life cycle starting with an {@code initial} state. From this
 * {@code initial} state, the implementation of this DAO can go into the {@code ready} state, if
 * everything works fine and as intended. The transition from the {@code initial} state into the
 * {@code ready} state triggers a {@link GremlinDAOReadyEvent}.
 * <p/>
 * During the lifetime of a {@code ready} DAO, changes can be made to the underlying database. Those
 * changes will transition this DAO into a {@code updating} state (leading to a {@link
 * GremlinDAOUpdatingEvent}). If the updating is finalized, the DAO is {@code ready} again. This
 * will issue a {@link GremlinDAOUpdatedEvent}.
 * <p/>
 * However, the DAO can always fail, and the resulting state will then be {@code failed}. A
 * transition into {@code failed}, will trigger a {@link GremlinDAOFailedEvent}. A DAO can always
 * recover, and move to {@code ready} again, also triggering the corresponding event.
 * <p/>
 * The DAO can not only directly communicate with the underlying database, but rely on another DAO
 * like {@link KnowledgeGraphDAOConfig}. This is the case for the {@link InMemoryGremlinDAO}
 * implementation of this interface.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KGGremlinDAO {

  /**
   * Returns the ability to traversal the knowledge graph.
   *
   * @return {@link GraphTraversalSource} for traversing the knowledge graph.
   */
  GraphTraversalSource traversal();

  /**
   * Gets the graph features of this Gremlin DAO.
   *
   * @return the graph features of this Gremlin DAO.
   */
  Features getFeatures();

}
