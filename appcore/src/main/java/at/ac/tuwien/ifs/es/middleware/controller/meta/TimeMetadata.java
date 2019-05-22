package at.ac.tuwien.ifs.es.middleware.controller.meta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
  private Long processingTimeInMs;

  public TimeMetadata(Instant ingestion) {
    this.ingestion = ingestion;
    this.release = Instant.now();
    this.processingTimeInMs = ingestion.until(release, ChronoUnit.MILLIS);
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

  public Long getProcessingTimeInMs() {
    return processingTimeInMs;
  }

  @Override
  public String toString() {
    return "TimeMetadata{" +
        "ingestion=" + ingestion +
        ", release=" + release +
        ", processingTimeInMs=" + processingTimeInMs +
        '}';
  }
}
