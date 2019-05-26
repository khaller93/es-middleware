package at.ac.tuwien.ifs.es.middleware.common.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;

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

  /**
   * Returns {@code true}, if transaction are supported, otherwise {@code false}.
   *
   * @return {@code true}, if transaction are supported, otherwise {@code false}.
   */
  boolean areTransactionsSupported();

  /**
   * Gets the {@link Features} that are supported by this Gremlin service.
   *
   * @return {@link Features} that are supported by this Gremlin service.
   */
  Features getFeatures();

  /**
   * If the used gremlin backend supports transactions, the the call get this method will start a new
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
   * transaction will be closed. If no transaction is supported, a call get this method allows
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
