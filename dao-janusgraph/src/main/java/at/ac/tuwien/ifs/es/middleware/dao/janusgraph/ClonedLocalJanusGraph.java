package at.ac.tuwien.ifs.es.middleware.dao.janusgraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.AbstractClonedGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.LiteralGraphSchema;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
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
@Component("LocalSyncingJanusGraph")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClonedLocalJanusGraph extends AbstractClonedGremlinDAO {

  private static final Logger logger = LoggerFactory.getLogger(ClonedLocalJanusGraph.class);

  private static final PGS schema = PGS.with("kind", "iri", "bnodeid",
      new LiteralGraphSchema(T.value, "datatype", "language"));

  private JanusGraph graph;

  @Autowired
  public ClonedLocalJanusGraph(ApplicationContext context, TaskExecutor taskExecutor,
      @Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      @Value("${janusgraph.dir}") String janusGraphDir) {
    super(context, sparqlDAO, schema, taskExecutor);
    this.setGraph(initGraphInstance(janusGraphDir));
  }

  private Graph initGraphInstance(String janusGraphDir) {
    logger.info("Started to initialize the Janusgraph.");
    graph = JanusGraphFactory.build().set("storage.backend", "berkeleyje")
        .set("storage.transactions", true)
        .set("storage.directory", new File(janusGraphDir).getAbsolutePath()).open();
    /* build and maintain IRI index */
    JanusGraphManagement mgmt = graph.openManagement();
    PropertyKey iriProperty = mgmt.getPropertyKey("iri");
    if (iriProperty == null) {
      iriProperty = mgmt.makePropertyKey("iri").dataType(String.class)
          .cardinality(Cardinality.SINGLE)
          .make();
    }
    if (mgmt.getGraphIndex("byIRI") == null) {
      mgmt.buildIndex("byIRI", Vertex.class).addKey(iriProperty)
          .unique().buildCompositeIndex();
    } else {
      try {
        mgmt.updateIndex(mgmt.getGraphIndex("byIRI"), SchemaAction.REINDEX).get();
      } catch (InterruptedException | ExecutionException e) {
        logger.error("Updating the IRI index failed. {}", e.getMessage());
      }
    }
    /* build and maintain version index */
    PropertyKey versionProperty = mgmt.getPropertyKey("version");
    if (versionProperty == null) {
      versionProperty = mgmt.makePropertyKey("version").dataType(Long.class)
          .cardinality(Cardinality.SINGLE).make();
    }
    if (mgmt.getGraphIndex("byVersion") == null) {
      mgmt.buildIndex("byVersion", Vertex.class).addKey(versionProperty).buildCompositeIndex();
    } else {
      try {
        mgmt.updateIndex(mgmt.getGraphIndex("byVersion"), SchemaAction.REINDEX).get();
      } catch (InterruptedException | ExecutionException e) {
        logger.error(". {}", e.getMessage());
      }
    }
    mgmt.commit();
    /* wait for index to be ready */
    try {
      GraphIndexStatusWatcher byIRIWatcher = ManagementSystem.awaitGraphIndexStatus(graph, "byIRI")
          .status(SchemaStatus.ENABLED);
      GraphIndexStatusWatcher byVersionWatcher = ManagementSystem
          .awaitGraphIndexStatus(graph, "byVersion").status(SchemaStatus.ENABLED);
      byIRIWatcher.call();
      byVersionWatcher.call();
    } catch (InterruptedException e) {
      logger.error("Waiting for the availability of the IRI/version index failed. {}",
          e.getMessage());
    }
    logger.info("Finished initializing the setup of Janusgraph.");
    return graph;
  }

  @Override
  protected boolean areTransactionSupported() {
    return true;
  }

  @Override
  protected void onBulkLoadCompleted() {
    logger.info("Starting the reindexing of the IRI/version index.");
    JanusGraphManagement mgmt = graph.openManagement();
    try {
      mgmt.updateIndex(mgmt.getGraphIndex("byIRI"), SchemaAction.REINDEX).get();
    } catch (ExecutionException | InterruptedException e) {
      logger.error("Building the IRI index failed. {}", e);
    }
    try {
      mgmt.updateIndex(mgmt.getGraphIndex("byVersion"), SchemaAction.REINDEX).get();
    } catch (ExecutionException | InterruptedException e) {
      logger.error("Building the version index failed. {}", e);
    }
    mgmt.commit();
    try {
      GraphIndexStatusWatcher byIRIWatcher = ManagementSystem.awaitGraphIndexStatus(graph, "byIRI")
          .status(SchemaStatus.ENABLED);
      GraphIndexStatusWatcher byVersionWatcher = ManagementSystem.awaitGraphIndexStatus(graph, "byVersion")
          .status(SchemaStatus.ENABLED);
      byIRIWatcher.call();
      byVersionWatcher.call();
      logger.info("Reindexing of the IRI/version was successful.");
    } catch (InterruptedException e) {
      logger.error("Waiting for the availability of the version index failed. {}", e.getMessage());
    }
  }
}
