package at.ac.tuwien.ifs.es.middleware.service.analysis;

import java.io.File;
import javax.annotation.PreDestroy;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * This class provides a {@link DB} bean that can be used by the analysis services to store results
 * in a key-value store.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="https://jankotek.gitbooks.io/mapdb/content/">MapDB Documentation</a>
 * @see <a href="http://www.mapdb.org">MapDB Homepage</a>
 * @since 1.0
 */
@Service
public class MapDB {

  private final DB db;
  private final DB tempDB;

  @Autowired
  public MapDB(@Value("${esm.db.data.dir}") String dataDir) {
    File dataDirFile = new File(dataDir, "mapdb");
    if (!dataDirFile.exists()) {
      dataDirFile.mkdirs();
    } else if (!dataDirFile.isDirectory()) {
      throw new IllegalArgumentException(
          "The path for storing the analysis results must not refer to a non-directory.");
    }
    db = DBMaker.fileDB(new File(dataDirFile, "map.db")).closeOnJvmShutdown().transactionEnable()
        .make();
    tempDB = DBMaker.tempFileDB().closeOnJvmShutdown().make();
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

  @Bean(name = "temp-mapdb")
  public DB tempDB() {
    return tempDB;
  }

  @PreDestroy
  public void closeDown() {
    if (db != null) {
      db.close();
    }
  }

}
