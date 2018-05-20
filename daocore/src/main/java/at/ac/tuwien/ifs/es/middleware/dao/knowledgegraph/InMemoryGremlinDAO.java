package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This {@link KnowledgeGraphGremlinDAO} is a {@link Graph} in-memory. It reads in all relationships between
 * resources (ignoring literals) and loads them into a graph that makes it possible to query over it
 * with Gremlin.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("InMemoryGremlin")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class InMemoryGremlinDAO extends AbstractClonedGremlinDAO {

  public InMemoryGremlinDAO(@Autowired KnowledgeGraphDAO knowledgeGraphDAO) {
    super(knowledgeGraphDAO);
  }

  @Override
  public Graph newGraphInstance() {
    return TinkerGraph.open();
  }
}
