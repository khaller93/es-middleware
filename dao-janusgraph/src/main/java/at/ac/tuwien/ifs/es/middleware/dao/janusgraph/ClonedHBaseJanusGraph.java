package at.ac.tuwien.ifs.es.middleware.dao.janusgraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.DependsOn;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.AbstractClonedGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.LiteralGraphSchema;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.diskstorage.hbase.HBaseStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AbstractClonedGremlinDAO} using a local instance using a
 * HBase node.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="http://janusgraph.org/">JanusGraph</a>
 * @since 1.0
 */
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Component("HBaseSyncingJanusGraph")
@DependsOn(sparql = true)
public class ClonedHBaseJanusGraph extends AbstractClonedGremlinDAO {

  private static final Logger logger = LoggerFactory.getLogger(ClonedHBaseJanusGraph.class);

  private static final PGS schema = PGS.with("kind", "iri", "bnodeid",
      new LiteralGraphSchema(T.value, "datatype", "language"));

  private JanusGraph graph;

  /**
   * Creates a new {@link AbstractClonedGremlinDAO} with the given {@code knowledgeGraphDAO}.
   *
   * @param context to publish the events.
   * @param sparqlDAO that shall be used.
   */
  public ClonedHBaseJanusGraph(ApplicationContext context,
      @Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO, TaskExecutor taskExecutor) {
    super(context, sparqlDAO, schema, taskExecutor);
    this.setGraph(initGraphInstance());
  }

  private Graph initGraphInstance() {
    logger.info("Started to initialize the Janusgraph.");
    graph = JanusGraphFactory.build().set("storage.backend", "hbase").open();
    IndexUtils.index(graph);
    logger.info("Finished initializing the setup of Janusgraph.");
    return graph;
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
