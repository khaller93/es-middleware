package at.ac.tuwien.ifs.es.middleware.testutil;

import javax.annotation.PreDestroy;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * This class provides a memory {@link DB} for testing.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://jankotek.gitbooks.io/mapdb/content/">MapDB Documentation</a>
 * @see <a href="http://www.mapdb.org">MapDB Homepage</a>
 * @since 1.0
 */
@Component
public class MapDBDummy {

  private final DB db;

  @Autowired
  public MapDBDummy() {
    db = DBMaker.memoryDB().closeOnJvmShutdown().transactionEnable().make();
  }

  /**
   * Gets a {@link DB} that can be used to create a persistent key-value store.
   *
   * @return {@link DB} that can be used to create a persistent key-value store.
   */
  @Bean(name = "persistent-mapdb")
  public DB db() {
    return db;
  }

  @PreDestroy
  public void closeDown() {
    if (db != null) {
      db.close();
    }
  }

}
