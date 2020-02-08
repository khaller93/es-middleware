package at.ac.tuwien.ifs.es.middleware.dao.janusgraph;

import java.util.concurrent.ExecutionException;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.SchemaAction;
import org.janusgraph.core.schema.SchemaStatus;
import org.janusgraph.graphdb.database.management.GraphIndexStatusWatcher;
import org.janusgraph.graphdb.database.management.ManagementSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IndexUtils {

  private static final Logger logger = LoggerFactory.getLogger(IndexUtils.class);

  static void index(JanusGraph graph) {
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
    }
    mgmt.commit();
    /* wait for index to be ready */
    try {
      GraphIndexStatusWatcher byIRIWatcher = ManagementSystem.awaitGraphIndexStatus(graph, "byIRI")
          .status(SchemaStatus.ENABLED);
      byIRIWatcher.call();
    } catch (InterruptedException e) {
      logger.error("Waiting for the availability of the IRI/version index failed. {}",
          e.getMessage());
    }
  }

  static void updateIndex(JanusGraph graph) {
    logger.info("Starting the reindexing of the IRI/version index.");
    JanusGraphManagement mgmt = graph.openManagement();
    try {
      mgmt.updateIndex(mgmt.getGraphIndex("byIRI"), SchemaAction.REINDEX).get();
    } catch (ExecutionException | InterruptedException e) {
      logger.error("Building the IRI index failed. {}", e);
    }
    mgmt.commit();
    try {
      GraphIndexStatusWatcher byIRIWatcher = ManagementSystem.awaitGraphIndexStatus(graph, "byIRI")
          .status(SchemaStatus.ENABLED);
      byIRIWatcher.call();
      logger.info("Reindexing of the IRI/version was successful.");
    } catch (InterruptedException e) {
      logger.error("Waiting for the availability of the version index failed. {}", e.getMessage());
    }
  }
}
