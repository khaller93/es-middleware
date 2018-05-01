package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphSetupException;
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
 * An implementation of {@link GraphDbDAO} connecting to an embedded GraphDB instance.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("EmbeddedGraphDB")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EmbeddedGraphDbDAO extends GraphDbDAO {

  private static final Logger logger = LoggerFactory.getLogger(EmbeddedGraphDbDAO.class);

  private RepositoryManager repositoryManager;

  /**
   * Creates a {@link GraphDbDAO} with the given repository configuration and repository location.
   */
  public EmbeddedGraphDbDAO(@Value("${graphdb.embedded.location}") String location,
      @Value("${graphdb.embedded.config.path}") String repositoryConfig,
      @Autowired ApplicationContext context) throws KnowledgeGraphSetupException {
    this.repositoryManager = new LocalRepositoryManager(new File(location));
    try {
      this.repositoryManager.initialize();
      this.initGraphDb(prepareRepository(repositoryManager, repositoryConfig), context);
    } catch (RepositoryException re) {
      throw new KnowledgeGraphSetupException(re);
    }
  }

  private static Repository prepareRepository(RepositoryManager repositoryManager,
      String repositoryConfig) {
    if (repositoryConfig == null || repositoryConfig.isEmpty()) {
      throw new KnowledgeGraphSetupException(
          "A 'graphdb.embedded.config.path' property must be given and point to the configuration file of the repository that should be built in an embedded GraphDB instance.");
    }
    File repoConfigurationFile = new File(repositoryConfig);
    if (repoConfigurationFile.exists() && repoConfigurationFile.isFile()) {
      try (InputStream configIn = new FileInputStream(repoConfigurationFile)) {
        Model configModel = Rio.parse(configIn, RepositoryConfigSchema.NAMESPACE, RDFFormat.TURTLE);
        Resource repositoryNode = Models
            .subject(configModel.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY))
            .orElseThrow(() -> new KnowledgeGraphSetupException(
                String.format("The given configuration '%s' is invalid.", repositoryConfig)));
        Literal repoId = Models.objectLiteral(
            configModel.filter(repositoryNode, RepositoryConfigSchema.REPOSITORYID, null))
            .orElseThrow(() -> new KnowledgeGraphSetupException(
                String.format(
                    "The repository id (%s) is missing in the configuration '%s' is invalid.",
                    RepositoryConfigSchema.REPOSITORYID, repositoryConfig)));
        repositoryManager.addRepositoryConfig(RepositoryConfig.create(configModel, repositoryNode));
        return repositoryManager.getRepository(repoId.getLabel());
      } catch (IOException | RDFParseException | UnsupportedRDFormatException e) {
        throw new KnowledgeGraphSetupException(
            String
                .format("The given configuration file '%s' could not be read in: %s",
                    repositoryConfig, e.getMessage()));
      }
    } else {
      throw new KnowledgeGraphSetupException(
          String.format(
              "The 'graphdb.embedded.config.path' property '%s' must point to the configuration file of the repository.",
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
