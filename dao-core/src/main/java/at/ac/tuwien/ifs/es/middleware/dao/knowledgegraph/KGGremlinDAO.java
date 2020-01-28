package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;

/**
 * This interface provides methods for traversing the knowledge graph using Gremlin.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KGGremlinDAO extends KGDAO {

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
