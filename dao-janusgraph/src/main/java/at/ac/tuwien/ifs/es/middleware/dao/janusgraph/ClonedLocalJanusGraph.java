package at.ac.tuwien.ifs.es.middleware.dao.janusgraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.AbstractClonedGremlinDAO;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AbstractClonedGremlinDAO} using a local JanusGraph instance
 * that is persisted on the local file system.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="http://janusgraph.org/">JanusGraph</a>
 * @since 1.0
 */
@Lazy
@Component("LocalSyncingJanusGraph")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClonedLocalJanusGraph extends AbstractClonedGremlinDAO {

  @Value("${janusgraph.dir}")
  private String janusGraphDir;

  @Autowired
  public ClonedLocalJanusGraph(@Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO) {
    super(sparqlDAO);
  }

  @Override
  public Graph initGraphInstance() {
    JanusGraph graph = JanusGraphFactory.build().set("storage.backend", "berkeleyje")
        .set("storage.directory", new File(janusGraphDir).getAbsolutePath()).open();
    return graph;
  }
}