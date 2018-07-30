package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.LiteralGraphSchema;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema.PGS;
import java.util.Optional;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class PropertyGraphSchemaTests {

  private Graph graph;

  private PGS schema1 = PGS.with("kind", T.id, T.id,
      new LiteralGraphSchema(T.value, "datatype", "language"));

  @Before
  public void setUp() {
    graph = TinkerGraph.open();
    Vertex guitarVertex = graph
        .addVertex(T.id,
            "test:guitar", "version", 1L, schema1.kind().identifier(), "iri");
    /*Vertex guitarLabelVertex = graph
        .addVertex(schema1.getLiteralGraphSchema().getValueProptertyName(), "Guitar",
            schema1.getLiteralGraphSchema().getDatatypePropertyName(), "xsd:string",
            schema1.getLiteralGraphSchema().getLanguagePropertyName(), "en");
    guitarVertex.addEdge("rdfs:label", guitarLabelVertex);*/
    Vertex ukeleleVertex = graph
        .addVertex(schema1.iri().identifier(), "test:ukelele",
            "version", 1L, schema1.kind().identifier(), "iri");
    /*Vertex ukeleleLabelVertex = graph
        .addVertex(schema1.getLiteralGraphSchema().getValueProptertyName(), "Ukelele",
            schema1.getLiteralGraphSchema().getDatatypePropertyName(), "xsd:string",
            schema1.getLiteralGraphSchema().getLanguagePropertyName(), "en");
    guitarVertex.addEdge("rdfs:label", ukeleleLabelVertex);*/
    Vertex fluteVertex = graph
        .addVertex(schema1.iri().identifier(), "test:flute", "version", 1L,
            schema1.kind().identifier(), "iri");
    /*Vertex fluteLabelVertex = graph
        .addVertex(schema1.getLiteralGraphSchema().getValueProptertyName(), "Flute",
            schema1.getLiteralGraphSchema().getDatatypePropertyName(), "xsd:string",
            schema1.getLiteralGraphSchema().getLanguagePropertyName(), "en");
    guitarVertex.addEdge("rdfs:label", fluteLabelVertex);*/
    guitarVertex.addEdge("test:similar", ukeleleVertex);
  }

  @Test
  public void fetchGuitarWithSchemaIRIId_mustReturnGuitarVertex() {
    Optional<Vertex> guitarVertex = graph.traversal().V()
        .has((T) schema1.iri().identifier(), "test:guitar").tryNext();
    assertTrue(guitarVertex.isPresent());
    System.out.println(">>" + graph.traversal().V().has("~id","test:guitar").next());
    System.out.println(">>" + T.id.apply(graph.traversal().V().has("~id","test:guitar").next()));
  }

}
