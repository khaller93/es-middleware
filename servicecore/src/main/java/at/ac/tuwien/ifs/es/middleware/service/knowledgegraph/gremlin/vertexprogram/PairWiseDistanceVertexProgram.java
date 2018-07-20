package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.vertexprogram;

import java.util.Set;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer.Persist;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer.ResultGraph;
import org.apache.tinkerpop.gremlin.process.computer.Memory;
import org.apache.tinkerpop.gremlin.process.computer.MessageScope;
import org.apache.tinkerpop.gremlin.process.computer.Messenger;
import org.apache.tinkerpop.gremlin.process.computer.VertexProgram;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 *
 */
public class PairWiseDistanceVertexProgram implements VertexProgram<Integer> {

  @Override
  public void setup(Memory memory) {

  }

  @Override
  public void execute(Vertex vertex, Messenger<Integer> messenger, Memory memory) {

  }

  @Override
  public boolean terminate(Memory memory) {
    return false;
  }

  @Override
  public Set<MessageScope> getMessageScopes(Memory memory) {
    return null;
  }

  @Override
  public VertexProgram<Integer> clone() {
    return null;
  }

  @Override
  public ResultGraph getPreferredResultGraph() {
    return null;
  }

  @Override
  public Persist getPreferredPersist() {
    return null;
  }
}
