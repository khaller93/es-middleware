package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOSetupException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@link RDF4JSparqlDAO} connecting to a remote GraphDB instance.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://ontotext.com/products/graphdb/">Ontotext GraphDB</a>
 * @since 1.0
 */
@Lazy
@Component("RemoteGraphDB")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RemoteGraphDbDAO extends RDF4JSparqlDAO implements GraphDbSparqlDAO {

  private static final Logger logger = LoggerFactory.getLogger(RemoteGraphDbDAO.class);

  /**
   * Creates a {@link RDF4JSparqlDAO} with the given location conf.
   */
  @Autowired
  public RemoteGraphDbDAO(@Value("${graphdb.address}") String address,
      @Value("${graphdb.repository.id}") String repositoryId,
      @Value("${graphdb.repository.username#{null}") String username,
      @Value("${graphdb.repository.password#{null}") String password) throws KGDAOSetupException {
    HTTPRepository repository = new HTTPRepository(address, repositoryId);
    if (username != null && !username.isEmpty()) {
      if (password != null && !password.isEmpty()) {
        repository.setUsernameAndPassword(username, password);
      } else {
        repository.setUsernameAndPassword(username, "");
        logger.warn("No password set for the username '{}' to access GraphDB.", username);
      }
    }
    this.init(repository);
  }

}
