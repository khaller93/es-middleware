package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.AbstractDAOEvent;
import java.time.Instant;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when data was changed in the underlying database and the SPARQL
 * DAO incorporates these changes right now. It is an {@link ApplicationEvent} that * an {@link
 * org.springframework.context.ApplicationListener} can listen to.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLDAOUpdatingEvent extends AbstractDAOEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public SPARQLDAOUpdatingEvent(Object source) {
    super(source, Instant.now());
  }

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   * @param timestamp the {@link Instant}, when the even occured (never {@code null})
   */
  public SPARQLDAOUpdatingEvent(Object source, Instant timestamp) {
    super(source, timestamp);
  }
}
