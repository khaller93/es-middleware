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
import org.springframework.context.ApplicationContext;
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

  @Autowired
  public ClonedLocalJanusGraph(ApplicationContext context,
      @Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      @Value("${janusgraph.dir}") String janusGraphDir) {
    super(context, sparqlDAO);
    this.setGraph(initGraphInstance(janusGraphDir));
  }

  private Graph initGraphInstance(String janusGraphDir) {
    JanusGraph graph = JanusGraphFactory.build().set("storage.backend", "berkeleyje")
        .set("storage.transactions", true)
        .set("storage.directory", new File(janusGraphDir).getAbsolutePath()).open();
    JanusGraphManagement mgmt = graph.openManagement();
    Iterable<PropertyKey> relationTypes = mgmt.getRelationTypes(PropertyKey.class);
    for (PropertyKey key : relationTypes) {
      System.out.println(">>>" + key);
    }
    return graph;
  }
}
