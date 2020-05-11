package at.ac.tuwien.ifs.es.middleware.dao.janusgraph;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Configuration
public class CassandraConfig {

  @Value("${cassandra.keyspace:esm}")
  private String keySpace;

  @Value("${cassandra.hostname}")
  private String hostname;

  @Value("${cassandra.id-suffix:#{null}}")
  private String idSuffix;

  public String getKeySpace() {
    return keySpace;
  }

  public String getHostname() {
    return hostname;
  }

  public String getIdSuffix() {
    return idSuffix;
  }
}
