package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.*;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.ClonedInMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
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
 * like {@link KnowledgeGraphDAOConfig}. This is the case for the {@link ClonedInMemoryGremlinDAO}
 * implementation of this interface.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KGGremlinDAO {

  /**
   * Gets the current {@link KGDAOStatus} of this DAO.
   *
   * @return current {@link KGDAOStatus} of this DAO.
   */
  KGDAOStatus getGremlinStatus();

  /**
   * Gets the graph features of this Gremlin DAO.
   *
   * @return the graph features of this Gremlin DAO.
   */
  Features getFeatures();

  /**
   * Returns the ability to traversal the knowledge graph.
   *
   * @return {@link GraphTraversalSource} for traversing the knowledge graph.
   */
  GraphTraversalSource traversal();

  /**
   * If the used gremlin backend supports transactions, the the call of this method will start a new
   * transaction for the current thread. If no transactions are supported, you can expect only a
   * serialized access to the backend.
   */
  void lock();

  /**
   * Commits the changes, if the gremlin backend supports transactions. Otherwise, this method is
   * expected to do nothing.
   */
  void commit();

  /**
   * Rollbacks the made changes in the opened transaction for gremlin backend that support
   * transactions. Otherwise, this method is expected to do nothing.
   */
  void rollback();

  /**
   * If the used gremlin backend supports transactions and there is an open transaction, this
   * transaction will be closed. If no transaction is supported, a call of this method allows
   * another thread to lock this gremlin DAO.
   */
  void unlock();

  /**
   * Gets the property graph schema for representing the RDF data. The returned schema must not be
   * null.
   *
   * @return the property graph schema for representing the RDF data. The returned schema must not
   * be null.
   */
  PGS getPropertyGraphSchema();

}
