package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.LiteralGraphSchema;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
@Component("ClonedInMemoryGremlin")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClonedInMemoryGremlinDAO extends AbstractClonedGremlinDAO {

  private static final PGS schema = PGS.with("kind", T.id, T.id,
      new LiteralGraphSchema(T.value, "datatype", "language"));

  @Autowired
  public ClonedInMemoryGremlinDAO(ApplicationContext context,
      @Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO) {
    super(context, sparqlDAO, schema);
    this.setGraph(TinkerGraph.open());
  }
}
