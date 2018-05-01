package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.text.StringSubstitutor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This {@link GremlinDAO} is a {@link Graph} in-memory. It reads in all relationships between
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
public class InMemoryGremlinDAO implements GremlinDAO {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryGremlinDAO.class);

  private static final int LOAD_LIMIT = 1000;

  private static final String ALL_STATEMENTS_QUERY = "SELECT DISTINCT ?s ?p ?o WHERE {\n"
      + "  ?s ?p ?o .\n"
      + "  FILTER (!isLiteral(?o)) .\n"
      + "}\n"
      + "OFFSET ${offset}\n"
      + "LIMIT ${limit}";

  private Graph graph;
  private KnowledgeGraphDAO knowledgeGraphDAO;

  public InMemoryGremlinDAO(@Autowired KnowledgeGraphDAO knowledgeGraphDAO) {
    this.knowledgeGraphDAO = knowledgeGraphDAO;
  }

  @PostConstruct
  public void setUp() {
    this.graph = constructGremlinGraphFromKnowledgeGraph();
  }

  /**
   * Constructs the in-memory graph and loads the statements from the {@link KnowledgeGraphDAO}.
   *
   * @return {@link Graph} that contains the loaded statements from the {@link KnowledgeGraphDAO}.
   */
  private Graph constructGremlinGraphFromKnowledgeGraph() {
    logger.debug("Starts to construct an in-memory graph.");
    Graph newGraph = TinkerFactory.createModern();
    Map<BlankNodeOrIRI, Vertex> recognizedNodes = new HashMap<>();
    List<Map<String, RDFTerm>> values;
    int offset = 0;
    do {
      Map<String, String> valuesMap = new HashMap<>();
      valuesMap.put("offset", String.valueOf(offset));
      valuesMap.put("limit", String.valueOf(LOAD_LIMIT));
      values = ((SelectQueryResult) knowledgeGraphDAO
          .query(new StringSubstitutor(valuesMap).replace(ALL_STATEMENTS_QUERY), true)).value();
      for (Map<String, RDFTerm> row : values) {
        BlankNodeOrIRI sResource = (BlankNodeOrIRI) row.get("s");
        Vertex sVertex = recognizedNodes
            .compute(sResource, (blankNodeOrIRI, vertex) -> vertex != null ? vertex
                : newGraph.addVertex(T.id, BlankOrIRIJsonUtil.stringValue(sResource)));
        BlankNodeOrIRI oResource = (BlankNodeOrIRI) row.get("o");
        Vertex oVertex = recognizedNodes
            .compute(sResource, (blankNodeOrIRI, vertex) -> vertex != null ? vertex
                : newGraph.addVertex(T.id, BlankOrIRIJsonUtil.stringValue(oResource)));
        BlankNodeOrIRI property = (BlankNodeOrIRI) row.get("p");
        sVertex.addEdge(BlankOrIRIJsonUtil.stringValue(property), oVertex);
      }
      logger.debug("Loaded {} statements from the knowledge graph {}.", offset + values.size(),
          knowledgeGraphDAO.getClass().getSimpleName());
      offset += LOAD_LIMIT;
    } while (!values.isEmpty() && values.size() < 1000);
    return newGraph;
  }

  @Override
  public GraphTraversalSource traverse() {
    return graph.traversal();
  }

  @Override
  public Features getFeatures() {
    return graph.features();
  }

  @PreDestroy
  public void tearDown() {
    try {
      if (graph != null) {
        graph.close();
      }
    } catch (Exception e) {
      logger.error("Failed to close the InMemoryGremlin graph. {}", e.getMessage());
    }
  }
}
