package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
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

  private KnowledgeGraphGremlinDAO knowledgeGraphGremlinDAO;

  public SimpleGremlinService(@Autowired KnowledgeGraphDAO knowledgeGraphDAO) {
    this.knowledgeGraphGremlinDAO = knowledgeGraphDAO.getGremlinDAO();
  }

  @Override
  public GraphTraversalSource traversal() {
    return knowledgeGraphGremlinDAO.traversal();
  }
}
