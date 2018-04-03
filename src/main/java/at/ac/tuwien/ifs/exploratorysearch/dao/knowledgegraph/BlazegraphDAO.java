package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph;

import java.util.List;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of {@link KnowledgeGraphDAO} for blazegraph triplestore.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://www.blazegraph.com/">Blazegraph</a>
 * @since 1.0
 */
@Lazy
@Component("Blazegraph")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BlazegraphDAO extends AbstractKnowledgeGraphDAO {

  private static final Logger logger = LoggerFactory.getLogger(BlazegraphDAO.class);

  private String queryEndpointURL;

  public BlazegraphDAO(@Value("${blazegraph.queryEndpointURL}") String queryEndpointURL) {
    init(new SPARQLRepository(queryEndpointURL));
  }
}
