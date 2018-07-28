package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.vertexprogram;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer.Persist;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer.ResultGraph;
import org.apache.tinkerpop.gremlin.process.computer.Memory;
import org.apache.tinkerpop.gremlin.process.computer.MemoryComputeKey;
import org.apache.tinkerpop.gremlin.process.computer.MessageScope;
import org.apache.tinkerpop.gremlin.process.computer.Messenger;
import org.apache.tinkerpop.gremlin.process.computer.VertexComputeKey;
import org.apache.tinkerpop.gremlin.process.computer.VertexProgram;
import org.apache.tinkerpop.gremlin.process.computer.util.AbstractVertexProgramBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.Operator;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;

/**
 * This vertex program computes the degree (incoming, outgoing edges) for all vertices. The number
 * of incoming edges indicates popularity.
 * <p/>
 * Per default only the incoming edges are counted.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class DegreeVertexProgram implements VertexProgram<Long> {

  public static final String DEGREE = "esm.gremlin.degreeVertexProgram.degree";
  public static final String DEGREE_IS_COMPUTED = "esm.gremlin.degreeVertexProgram.iscomputed";
  // Configuration keys
  public static final String DEGREE_CONFIG_IN = "esm.gremlin.degreeVertexProgram.conf.incoming.edges";
  public static final String DEGREE_CONFIG_OUT = "esm.gremlin.degreeVertexProgram.conf.outgoing.edges";

  private boolean countIncomingEdges = true;
  private boolean countOutgoingEdges = false;

  private Set<VertexComputeKey> vertexComputeKeys;
  private Set<MemoryComputeKey> memoryComputeKeys;

  @Override
  public void loadState(Graph graph, Configuration configuration) {
    if (configuration.containsKey(DEGREE_CONFIG_IN)) {
      this.countIncomingEdges = configuration.getBoolean(DEGREE_CONFIG_IN);
    }
    if (configuration.containsKey(DEGREE_CONFIG_OUT)) {
      this.countOutgoingEdges = configuration.getBoolean(DEGREE_CONFIG_OUT);
    }
    if (!this.countIncomingEdges && !this.countOutgoingEdges) {
      this.countIncomingEdges = true;
    }
  }

  @Override
  public void storeState(Configuration configuration) {
    VertexProgram.super.storeState(configuration);
    configuration.setProperty(DEGREE_CONFIG_IN, this.countIncomingEdges);
    configuration.setProperty(DEGREE_CONFIG_OUT, this.countOutgoingEdges);
    this.vertexComputeKeys = Sets.newHashSet(
        Collections.singleton(VertexComputeKey.of(DEGREE, false)));
    this.memoryComputeKeys = Sets
        .newHashSet(MemoryComputeKey.of(DEGREE_IS_COMPUTED, Operator.sum, true, true),
            MemoryComputeKey.of(DEGREE_CONFIG_IN, Operator.and, true, true),
            MemoryComputeKey.of(DEGREE_CONFIG_OUT, Operator.and, true, true)
        );
  }

  @Override
  public void setup(Memory memory) {
    memory.set(DEGREE_IS_COMPUTED, 0);
  }

  @Override
  public void execute(Vertex vertex, Messenger<Long> messenger, Memory memory) {
    long edgeCount = 0;
    if (memory.<Boolean>get(DEGREE_CONFIG_IN)) {
      for (Iterator<Edge> edgesIterator = vertex.edges(Direction.IN); edgesIterator.hasNext();
          edgesIterator.next()) {
        edgeCount += 1;
      }
    }
    if (memory.<Boolean>get(DEGREE_CONFIG_OUT)) {
      for (Iterator<Edge> edgesIterator = vertex.edges(Direction.OUT); edgesIterator.hasNext();
          edgesIterator.next()) {
        edgeCount += 1;
      }
    }
    vertex.property(Cardinality.single, DEGREE, edgeCount);
  }

  @Override
  public Set<MessageScope> getMessageScopes(Memory memory) {
    return Sets.newHashSet(MessageScope.Local.of(__::outE));
  }

  @Override
  public VertexProgram<Long> clone() {
    try {
      return (DegreeVertexProgram) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  @Override
  public Set<VertexComputeKey> getVertexComputeKeys() {
    return vertexComputeKeys;
  }

  @Override
  public Set<MemoryComputeKey> getMemoryComputeKeys() {
    return memoryComputeKeys;
  }

  @Override
  public ResultGraph getPreferredResultGraph() {
    return ResultGraph.NEW;
  }

  @Override
  public Persist getPreferredPersist() {
    return Persist.VERTEX_PROPERTIES;
  }

  @Override
  public void workerIterationEnd(Memory memory) {
    memory.set(DEGREE_IS_COMPUTED, 1);
  }

  @Override
  public boolean terminate(Memory memory) {
    return memory.<Integer>get(DEGREE_IS_COMPUTED) == 1;
  }

  public static Builder build() {
    return new Builder();
  }

  public final static class Builder extends AbstractVertexProgramBuilder<Builder> {

    public Builder() {
      super(DegreeVertexProgram.class);
    }
  }

}
