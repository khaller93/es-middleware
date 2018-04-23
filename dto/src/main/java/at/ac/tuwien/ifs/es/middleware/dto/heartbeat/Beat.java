package at.ac.tuwien.ifs.es.middleware.dto.heartbeat;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.Instant;

/**
 * This DTO represents a heart beat.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class Beat {

  private String name;
  private String version;
  private Instant timestamp;

  public Beat(String name, String version) {
    this.name = name;
    this.version = version;
    this.timestamp = Instant.now();
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "Beat{" +
        "name='" + name + '\'' +
        ", version=" + version +
        ", timestamp=" + timestamp +
        '}';
  }
}
