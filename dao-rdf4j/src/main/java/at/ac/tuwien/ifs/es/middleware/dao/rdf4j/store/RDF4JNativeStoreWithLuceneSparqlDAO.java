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
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of {@link RDF4JSparqlDAO}. It uses the native storage of RDF4J,
 * which can handle data in the range of 100 million triples. A Lucene index is applied to the
 * storage text, whereby the index is persisted to the disk.
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
  public RDF4JNativeStoreWithLuceneSparqlDAO(@Value("${rdf4j.dir}") String dataDir) {
    logger.debug("Initiating the RDF4J memory storage, where text is indexed with Lucene.");
    File dir = new File(dataDir);
    File nativeStoreDir = new File(dir, "storage");
    /* prepare native storage data dir */
    if (!nativeStoreDir.exists()) {
      boolean createdDirs = nativeStoreDir.mkdirs();
      if (createdDirs) {
        logger
            .trace("Directories for native storage '{}' created.",
                nativeStoreDir.getAbsolutePath());
      } else {
        logger.trace("Could not create native storage for path '{}'.",
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
    logger.debug("RDF4J native storage, where text is indexed with Lucene, is ready.");
  }

}
