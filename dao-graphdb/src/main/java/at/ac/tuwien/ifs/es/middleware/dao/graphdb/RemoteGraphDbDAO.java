package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSetupException;
import javax.annotation.PostConstruct;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@link GraphDbDAO} connecting to a remote GraphDB instance.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("RemoteGraphDB")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RemoteGraphDbDAO extends GraphDbDAO {

  @Value("${graphdb.address}")
  private String address;
  @Value("${graphdb.repository.id}")
  private String repositoryId;

  /**
   * Creates a {@link GraphDbDAO} with the given location configuration.
   */
  @Autowired
  public RemoteGraphDbDAO(ApplicationContext context) throws KnowledgeGraphSetupException {
    super(context);
  }

  @PostConstruct
  public void setUp() {
    this.init(new HTTPRepository(address, repositoryId));
  }
}
