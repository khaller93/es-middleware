package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin;

import java.util.concurrent.locks.Lock;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;
import org.apache.tinkerpop.gremlin.structure.Transaction;

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
   * Gets a {@link Transaction} for this Gremlin DAO.
   *
   * @return a {@link Transaction} for this Gremlin DAO.
   */
  Transaction getTransaction();

  /**
   * Gets a lock for accessing this Gremlin DAO. This lock can be used for Gremlin DAOs that have no
   * support for transaction.
   *
   * @return a lock for accessing this Gremlin DAO.
   */
  Lock getLock();

  /**
   * Gets the {@link Features} that are supported by this Gremlin service.
   *
   * @return {@link Features} that are supported by this Gremlin service.
   */
  Features getFeatures();
}
