package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import java.util.concurrent.locks.Lock;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is a simple implementation of {@link GremlinService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Primary
@Service
public class SimpleGremlinService implements GremlinService{

  private KGGremlinDAO gremlinDAO;

  @Autowired
  public SimpleGremlinService(KGGremlinDAO gremlinDAO) {
    this.gremlinDAO = gremlinDAO;
  }

  @Override
  public GraphTraversalSource traversal() {
    return gremlinDAO.traversal();
  }

  @Override
  public boolean areTransactionsSupported() {
    return getFeatures().graph().supportsTransactions();
  }

  @Override
  public Transaction getTransaction() {
    return gremlinDAO.getTransaction();
  }

  @Override
  public Lock getLock() {
    return gremlinDAO.getLock();
  }

  @Override
  public Features getFeatures() {
    return gremlinDAO.getFeatures();
  }
}
