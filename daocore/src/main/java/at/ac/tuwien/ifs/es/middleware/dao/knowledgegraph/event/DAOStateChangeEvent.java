package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.ApplicationEvent;

/**
 * This class represents an event indicating a state transition of a {@link
 * at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAO}. The event maintains information about
 * the previous status and the current one. Moreover, a transition gets an eventId. This eventId is
 * used to keep track of transitions that are caused by one single event (e.g. execution of a SPARQL
 * 'Insert' query).
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class DAOStateChangeEvent extends ApplicationEvent {

  private final static AtomicLong eventIdCounter = new AtomicLong(
      (long) (Math.random() * Long.MAX_VALUE));

  private static long generateEventId() {
    return eventIdCounter.getAndUpdate(l -> (l == Long.MAX_VALUE ? 0 : l + 1));
  }

  private long eventId;
  private KGDAOStatus status;
  private KGDAOStatus prevStatus;
  private Instant timestamp;

  /**
   * Create a new state transition event for a DAO and creates an unique event id for it.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   * @param newStatus the new {@link KGDAOStatus} that triggered this state change event.
   * @param prevStatus the {@link KGDAOStatus} before this transition.
   * @param timestamp the {@link Instant}, when the even occured (never {@code null})
   */
  public DAOStateChangeEvent(KGDAO source, KGDAOStatus newStatus, KGDAOStatus prevStatus,
      Instant timestamp) {
    this(source, generateEventId(), newStatus, prevStatus, timestamp);
  }

  /**
   * Create a new state transition event for a DAO.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   * @param newStatus the new {@link KGDAOStatus} that triggered this state change event.
   * @param prevStatus the {@link KGDAOStatus} before this transition.
   * @param timestamp the {@link Instant}, when the even occured (never {@code null})
   */
  public DAOStateChangeEvent(KGDAO source, long eventId, KGDAOStatus newStatus,
      KGDAOStatus prevStatus, Instant timestamp) {
    super(source);
    checkArgument(newStatus != null, "The new status must be specified.");
    checkArgument(prevStatus != null, "The previous status must be specified.");
    checkArgument(timestamp != null, "Given timestamp must not be null.");
    this.eventId = eventId;
    this.status = newStatus;
    this.prevStatus = prevStatus;
    this.timestamp = timestamp;
  }

  /**
   * Gets the id of the event that caused this transition.
   *
   * @return the id of the event that caused this transition.
   */
  public long getEventId() {
    return eventId;
  }

  /**
   * Gets the {@link KGDAOStatus} to which the source transitioned.
   *
   * @return {@link KGDAOStatus} to which the source transitioned.
   */
  public KGDAOStatus getStatus() {
    return status;
  }

  /**
   * Gets the {@link KGDAOStatus} from which the source transitioned.
   *
   * @return {@link KGDAOStatus} from which the source transitioned.
   */
  public KGDAOStatus getPreviousStatus() {
    return prevStatus;
  }

  /**
   * Gets the {@link Instant} timestamp of the event.
   *
   * @return {@link Instant} timestamp of the event.
   */
  public Instant getDAOTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "DAOStateChangeEvent{" +
        "eventId=" + eventId +
        ", status=" + status +
        ", prevStatus=" + prevStatus +
        ", timestamp=" + timestamp +
        '}';
  }
}
