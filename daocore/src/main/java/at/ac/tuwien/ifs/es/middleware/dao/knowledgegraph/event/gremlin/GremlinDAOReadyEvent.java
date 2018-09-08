package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.AbstractDAOEvent;
import java.time.Instant;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when the {@link KGGremlinDAO}
 * is ready for usage and no known changes were made to the data of the underlying database.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class GremlinDAOReadyEvent extends AbstractDAOEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public GremlinDAOReadyEvent(Object source) {
    super(source, Instant.now());
  }

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   * @param timestamp the {@link Instant}, when the even occured (never {@code null})
   */
  public GremlinDAOReadyEvent(Object source, Instant timestamp) {
    super(source, timestamp);
  }
}
