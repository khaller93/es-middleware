package at.ac.tuwien.ifs.es.middleware.dao.graphdb.unit;

import at.ac.tuwien.ifs.es.middleware.dao.graphdb.GraphDbSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSetupException;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOFailedStatus;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PreDestroy;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@link RDF4JSparqlDAO} connecting to an embedded GraphDB instance.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://ontotext.com/products/graphdb/">Ontotext GraphDB</a>
 * @since 1.0
 */
@Lazy
@Component("EmbeddedGraphDB")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EmbeddedGraphDbDAO extends RDF4JSparqlDAO implements GraphDbSparqlDAO {

  private static final Logger logger = LoggerFactory.getLogger(EmbeddedGraphDbDAO.class);

  private RepositoryManager repositoryManager;

  /**
   * Creates a {@link RDF4JSparqlDAO} with the given repository conf and repository location.
   */
  @Autowired
  public EmbeddedGraphDbDAO(ApplicationContext context,
      @Value("${graphdb.embedded.location}") String location,
      @Value("${graphdb.embedded.config.path}") String repositoryConfig)
      throws KnowledgeGraphSetupException {
    super(context);
    File locationFile = new File(location);
    if (!locationFile.exists()) {
      boolean createdDirs = locationFile.mkdirs();
      if (createdDirs) {
        logger
            .trace("Directories created for embedded GraphDB '{}'.",
                locationFile.getAbsolutePath());
      } else {
        logger
            .trace("Directories could not be created for embedded GraphDB '{}'.",
                locationFile.getAbsolutePath());
      }
    }
    /* setup embedded GraphDB */
    this.repositoryManager = new LocalRepositoryManager(locationFile);
    try {
      this.repositoryManager.initialize();
      this.init(prepareRepository(repositoryManager, repositoryConfig));
    } catch (RepositoryException re) {
      setStatus(new KGDAOFailedStatus("Triplestore could not be setup", re));
      throw new KnowledgeGraphSetupException(re);
    }
  }

  /**
   * Prepares the repository for the embedded GraphDB, using the given {@code repositoryConfig}.
   *
   * @param repositoryManager {@link RepositoryManager} that shall be used.
   * @param repositoryConfig path to the conf for the embedded GraphDB.
   * @return {@link Repository} representing the embedded GraphDB.
   */
  private static Repository prepareRepository(RepositoryManager repositoryManager,
      String repositoryConfig) {
    if (repositoryConfig == null || repositoryConfig.isEmpty()) {
      throw new KnowledgeGraphSetupException(
          "A 'graphdb.embedded.config.path' property must be given and point to the conf file of the repository that should be built in an embedded GraphDB instance.");
    }
    File repoConfigurationFile = new File(repositoryConfig);
    if (repoConfigurationFile.exists() && repoConfigurationFile.isFile()) {
      try (InputStream configIn = new FileInputStream(repoConfigurationFile)) {
        Model configModel = Rio.parse(configIn, RepositoryConfigSchema.NAMESPACE, RDFFormat.TURTLE);
        Resource repositoryNode = Models
            .subject(configModel.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY))
            .orElseThrow(() -> new KnowledgeGraphSetupException(
                String.format("The given conf '%s' is invalid.", repositoryConfig)));
        Literal repoId = Models.objectLiteral(
            configModel.filter(repositoryNode, RepositoryConfigSchema.REPOSITORYID, null))
            .orElseThrow(() -> new KnowledgeGraphSetupException(
                String.format(
                    "The repository id (%s) is missing in the conf '%s' is invalid.",
                    RepositoryConfigSchema.REPOSITORYID, repositoryConfig)));
        repositoryManager.addRepositoryConfig(RepositoryConfig.create(configModel, repositoryNode));
        return repositoryManager.getRepository(repoId.getLabel());
      } catch (IOException | RDFParseException | UnsupportedRDFormatException e) {
        throw new KnowledgeGraphSetupException(
            String
                .format("The given conf file '%s' could not be read in: %s",
                    repositoryConfig, e.getMessage()));
      }
    } else {
      throw new KnowledgeGraphSetupException(
          String.format(
              "The 'graphdb.embedded.config.path' property '%s' must point to the conf file of the repository.",
              repositoryConfig));
    }
  }

  @PreDestroy
  public void tearDown() {
    if (repositoryManager != null) {
      repositoryManager.shutDown();
    }
  }
}
