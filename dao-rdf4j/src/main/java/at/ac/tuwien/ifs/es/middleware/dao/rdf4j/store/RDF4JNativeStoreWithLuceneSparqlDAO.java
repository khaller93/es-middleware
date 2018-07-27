package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store;

import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.LuceneIndexedRDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JSparqlDAO;
import java.io.File;
import org.eclipse.rdf4j.sail.lucene.LuceneSail;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
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
 * This class is an implementation of {@link RDF4JSparqlDAO}. It uses the native store of RDF4J,
 * which can handle data in the range of 100 million triples. A Lucene index is applied to the store
 * text, whereby the index is persisted to the disk.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("RDF4JNativeStoreWithLucene")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RDF4JNativeStoreWithLuceneSparqlDAO extends RDF4JSparqlDAO implements
    LuceneIndexedRDF4JSparqlDAO {

  private static final Logger logger = LoggerFactory
      .getLogger(RDF4JNativeStoreWithLuceneSparqlDAO.class);

  @Autowired
  public RDF4JNativeStoreWithLuceneSparqlDAO(ApplicationContext applicationContext,
      @Value("${rdf4j.dir}") String dataDir) {
    super(applicationContext);
    setUp(dataDir);
  }

  public void setUp(String dataDir) {
    logger.debug("Initiating the RDF4J memory store, where text is indexed with Lucene.");
    File dir = new File(dataDir);
    File nativeStoreDir = new File(dir, "store");
    /* prepare native store data dir */
    if (!nativeStoreDir.exists()) {
      boolean createdDirs = nativeStoreDir.mkdirs();
      if (createdDirs) {
        logger
            .trace("Directories for native store '{}' created.", nativeStoreDir.getAbsolutePath());
      } else {
        logger.trace("Could not create native store for path '{}'.",
            nativeStoreDir.getAbsolutePath());
      }
    }
    /* prepare lucene data dir */
    File luceneDir = new File(dir, "lucene");
    if (!luceneDir.exists()) {
      boolean createdDirs = luceneDir.mkdirs();
      if (createdDirs) {
        logger.trace("Directories for lucene '{}' created.", luceneDir.getAbsolutePath());
      } else {
        logger
            .trace("Could not create directories for lucene '{}'.", luceneDir.getAbsolutePath());
      }
    }
    /* setup the sail */
    LuceneSail luceneSail = new LuceneSail();
    luceneSail.setParameter(LuceneSail.LUCENE_DIR_KEY, luceneDir.getAbsolutePath());
    luceneSail.setBaseSail(new NativeStore(nativeStoreDir));
    this.init(luceneSail);
    logger.debug("RDF4J native store, where text is indexed with Lucene, is ready.");
  }

}
