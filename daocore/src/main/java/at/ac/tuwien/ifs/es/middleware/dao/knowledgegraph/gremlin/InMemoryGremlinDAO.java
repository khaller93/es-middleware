package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * This class implements the {@link KGGremlinDAO} as a in-memory {@link TinkerGraph}. It makes use
 * of the abstract implementation {@link AbstractClonedGremlinDAO}, which implements all the
 * necessary steps for cloning relevant data from the {@link KnowledgeGraphDAOConfig}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("InMemoryGremlin")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class InMemoryGremlinDAO extends AbstractClonedGremlinDAO {


  @Autowired
  public InMemoryGremlinDAO(@Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO) {
    super(sparqlDAO);
  }

  @Override
  public Graph initGraphInstance() {
    return TinkerGraph.open();
  }

}
