package at.ac.tuwien.ifs.es.middleware.dao.janusgraph;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 *
 *
 * @author Kevin Haller
 * @since 1.0
 * @version 1.0
 */
@Lazy
@Configuration
public class CassandraConfig {

  @Value("${cassandra.keyspace:esm}")
  private String keySpace;

  @Value("${cassandra.hostname}")
  private String hostname;


  public String getKeySpace() {
    return keySpace;
  }

  public String getHostname() {
    return hostname;
  }
}
