package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event;

import static com.google.common.base.Preconditions.checkArgument;

import java.time.Instant;
import org.springframework.context.ApplicationEvent;

/**
 * This class represents an abstract DAO event.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractDAOEvent extends ApplicationEvent {

  private Instant timestamp;

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   * @param timestamp the {@link Instant}, when the even occured (never {@code null})
   */
  public AbstractDAOEvent(Object source, Instant timestamp) {
    super(source);
    checkArgument(timestamp != null, "Given timestamp must not be null.");
    this.timestamp = timestamp;
  }

  /**
   * Gets the {@link Instant} timestamp of the event.
   *
   * @return {@link Instant} timestamp of the event.
   */
  public Instant getDAOTimestamp() {
    return timestamp;
  }
}
