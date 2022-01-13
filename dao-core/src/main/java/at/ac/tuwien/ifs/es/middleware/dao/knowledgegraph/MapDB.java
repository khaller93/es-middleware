package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import org.mapdb.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

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
@Component
public class MapDB {

  private ApplicationContext context;
  private final String variant;

  @Autowired
  public MapDB(@Value("${esm.db.map:file}") String variant, ApplicationContext context) {
    this.variant = variant;
    this.context = context;
  }

  @Bean
  @Primary
  public DB getMapDB() {
    switch (variant) {
      case "memory":
        return context.getBean("mapdb-memory", DB.class);
      default:
        return context.getBean("mapdb-file", DB.class);
    }
  }

}
