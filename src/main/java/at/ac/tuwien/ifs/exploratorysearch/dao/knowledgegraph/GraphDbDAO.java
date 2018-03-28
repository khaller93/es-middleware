package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception.KnowledgeGraphSetupException;
import java.util.List;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@link KnowledgeGraphDAO} for the Ontotext GraphDB triplestore.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://ontotext.com/products/graphdb/">Ontotext GraphDB</a>
 * @since 1.0
 */
@Component("GraphDB")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GraphDbDAO extends AbstractKnowledgeGraphDAO {

  private static final Logger logger = LoggerFactory.getLogger(GraphDbDAO.class);

  /**
   * Creates a {@link GraphDbDAO} with the given location configuration.
   */
  public GraphDbDAO(
      @Value("${graphdb.queryEndpointURL}") String queryEndpointURL,
      @Value("${graphdb.updateEndpointURL}") String updateEndpointURL)
      throws KnowledgeGraphSetupException {
    super(new SPARQLRepository(queryEndpointURL,
        updateEndpointURL != null ? updateEndpointURL : queryEndpointURL));
  }

  @Override
  public List<String> searchFullText(String selectionQuery, String keyword, Long limit,
      Long offset) {
    //TODO: implement.
    return null;
  }

}
