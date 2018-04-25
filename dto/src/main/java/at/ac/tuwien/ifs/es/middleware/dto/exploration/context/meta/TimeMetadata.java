package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.meta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * This class maintains information about ingestion of the request, as well as release time. Thus,
 * it is possible for the client to infer processing time.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class TimeMetadata {

  private Instant ingestion;
  private Instant release;

  public TimeMetadata(Instant ingestion) {
    this.ingestion = ingestion;
    this.release = Instant.now();
  }

  @JsonCreator
  public TimeMetadata(@JsonProperty(value = "ingestion", required = true) Instant ingestion,
      @JsonProperty(value = "release", required = true) Instant release) {
    this.ingestion = ingestion;
    this.release = release;
  }

  public Instant getIngestion() {
    return ingestion;
  }

  public Instant getRelease() {
    return release;
  }

  @Override
  public String toString() {
    return "TimeMetadata{" +
        "ingestion=" + ingestion +
        ", release=" + release +
        '}';
  }
}
