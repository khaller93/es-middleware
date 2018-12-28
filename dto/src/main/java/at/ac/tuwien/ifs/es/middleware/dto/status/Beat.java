package at.ac.tuwien.ifs.es.middleware.dto.status;

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

  public Beat(String name, String version, Instant timestamp) {
    this.name = name;
    this.version = version;
    this.timestamp = timestamp;
  }

  /**
   * Creates a new heart beat representing an ok status of this middleware.
   *
   * @param name of the middleware
   * @param version of the middleware
   * @return a new heart beat representing an ok status of this middleware.
   */
  public static Beat ok(String name, String version) {
    return new Beat(name, version, Instant.now());
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
