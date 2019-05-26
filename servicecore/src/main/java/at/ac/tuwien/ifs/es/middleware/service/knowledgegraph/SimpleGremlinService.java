package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.common.knowledgegraph.GremlinService;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This is a simple implementation get {@link GremlinService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Primary
@Service
public class SimpleGremlinService implements GremlinService {

  private final KGGremlinDAO gremlinDAO;

  @Autowired
  public SimpleGremlinService(@Qualifier("getGremlinDAO") KGGremlinDAO gremlinDAO) {
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
  public Features getFeatures() {
    return gremlinDAO.getFeatures();
  }

  @Override
  public void lock() {
    gremlinDAO.lock();
  }

  @Override
  public void commit() {
    gremlinDAO.commit();
  }

  @Override
  public void rollback() {
    gremlinDAO.rollback();
  }

  @Override
  public void unlock() {
    gremlinDAO.unlock();
  }

  @Override
  public PGS getPropertyGraphSchema() {
    return gremlinDAO.getPropertyGraphSchema();
  }
}
