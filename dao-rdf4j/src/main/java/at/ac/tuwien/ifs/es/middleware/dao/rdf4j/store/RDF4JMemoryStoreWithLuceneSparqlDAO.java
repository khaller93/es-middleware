package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.store;

import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.LuceneIndexedRDF4JSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JSparqlDAO;
import org.eclipse.rdf4j.sail.lucene.LuceneSail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of {@link RDF4JSparqlDAO}. It uses the memory store provided by
 * the RDF4J framework. A lucene index of the stored text is kept in memory.
 * <p/>
 * This class is intended for testing, but not for production.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("RDF4JMemoryStoreWithLucene")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RDF4JMemoryStoreWithLuceneSparqlDAO extends RDF4JSparqlDAO implements
    LuceneIndexedRDF4JSparqlDAO {

  private final static Logger logger = LoggerFactory
      .getLogger(RDF4JMemoryStoreWithLuceneSparqlDAO.class);

  /**
   * Creates a new {@link RDF4JMemoryStoreWithLuceneSparqlDAO} in memory, where the text is indexed
   * with Lucene.
   *
   * @param context that shall be used to fetch the beans.
   */
  @Autowired
  public RDF4JMemoryStoreWithLuceneSparqlDAO(ApplicationContext context) {
    super(context);
    logger.debug("Initiating the RDF4J memory store, where text is indexed with Lucene.");
    LuceneSail luceneSail = new LuceneSail();
    luceneSail.setParameter(LuceneSail.LUCENE_RAMDIR_KEY, "true");
    luceneSail.setBaseSail(new MemoryStore());
    this.init(luceneSail);
    logger.debug("RDF4J memory store, where text is indexed with Lucene, is ready.");
  }

}
