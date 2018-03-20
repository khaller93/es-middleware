package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph;

import java.util.List;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of {@link AbstractKnowledgeGraphDAO}. This class can be used for
 * testing the overlying services without starting a standalone triplestore instance.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Component("MemoryDB")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RDF4JMemoryKnowledgeGraph extends AbstractKnowledgeGraphDAO {

  public RDF4JMemoryKnowledgeGraph() {
    super(new SailRepository(new MemoryStore()));
  }

  @Override
  public List<String> searchFullText(String selectionQuery, String keyword, Long limit,
      Long offset) {
    //TODO: implement
    return null;
  }
}
