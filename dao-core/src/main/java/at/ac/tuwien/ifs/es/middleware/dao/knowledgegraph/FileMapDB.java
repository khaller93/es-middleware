package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import java.io.File;
import javax.annotation.PreDestroy;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * This class provides a {@link DB} bean that can be used by the analysis services to store results
 * in a key-value store. The data is persisted to a file.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Component
public class FileMapDB {

  private final DB db;

  @Autowired
  public FileMapDB(@Value("${esm.db.data.dir}") String dataDir) {
    File dataDirFile = new File(dataDir, "mapdb");
    if (!dataDirFile.exists()) {
      dataDirFile.mkdirs();
    } else if (!dataDirFile.isDirectory()) {
      throw new IllegalArgumentException(
          "The path for storing the analysis results must not refer to a non-directory.");
    }
    db = DBMaker.fileDB(new File(dataDirFile, "map.db")).closeOnJvmShutdown().transactionEnable()
        .make();
  }

  /**
   * Gets a {@link DB} that can be used to create a persistent key-value store.
   *
   * @return {@link DB} that can be used to create a persistent key-value store.
   */
  @Bean(name = "esm.db.map.file")
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
