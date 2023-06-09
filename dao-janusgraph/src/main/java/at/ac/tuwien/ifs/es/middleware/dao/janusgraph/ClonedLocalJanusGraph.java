package at.ac.tuwien.ifs.es.middleware.dao.janusgraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DependsOn;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.AbstractClonedGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.LiteralGraphSchema;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import java.io.File;
import java.util.concurrent.ExecutionException;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.SchemaAction;
import org.janusgraph.core.schema.SchemaStatus;
import org.janusgraph.graphdb.database.management.GraphIndexStatusWatcher;
import org.janusgraph.graphdb.database.management.ManagementSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
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
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Component("LocalSyncingJanusGraph")
@DependsOn(sparql = true)
public class ClonedLocalJanusGraph extends AbstractClonedGremlinDAO {

  private static final Logger logger = LoggerFactory.getLogger(ClonedLocalJanusGraph.class);

  private static final PGS schema = PGS.with("kind", "iri", "bnodeid",
      new LiteralGraphSchema(T.value, "datatype", "language"));

  private JanusGraph graph;

  @Autowired
  public ClonedLocalJanusGraph(ApplicationContext context, TaskExecutor taskExecutor,
      @Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      @Value("${esm.db.data.dir}") String dataDir) {
    super(context, sparqlDAO, schema, taskExecutor);
    this.setGraph(initGraphInstance(dataDir));
  }

  private Graph initGraphInstance(String dataDir) {
    logger.info("Started to initialize the Janusgraph.");
    File dataDirFile = new File(dataDir, "janusgraph");
    if (!dataDirFile.exists()) {
      boolean success = dataDirFile.mkdirs();
    } else if (!dataDirFile.isDirectory()) {
      throw new IllegalArgumentException(
          "The path for storing the analysis results must not refer to a non-directory.");
    }
    graph = JanusGraphFactory.build().set("storage.backend", "berkeleyje")
        .set("storage.transactions", true)
        .set("storage.directory", dataDirFile.getAbsolutePath()).open();
    IndexUtils.index(graph);
    logger.info("Finished initializing the setup of Janusgraph.");
    return graph;
  }

  @Override
  public void update(long timestamp) throws KGDAOException {

  }

  @Override
  protected boolean areTransactionSupported() {
    return true;
  }

  @Override
  protected void onBulkLoadCompleted() {
    IndexUtils.updateIndex(graph);
  }
}
