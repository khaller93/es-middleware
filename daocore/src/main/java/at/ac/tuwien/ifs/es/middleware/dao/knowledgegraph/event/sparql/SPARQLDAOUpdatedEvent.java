package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.AbstractDAOEvent;
import java.time.Instant;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when data has been changed in the underlying database and the
 * publishing {@link KnowledgeGraphDAOConfig} has incorporated those changes. It is an {@link
 * ApplicationEvent} that an {@link org.springframework.context.ApplicationListener} can listen to.
 * <p/>
 * //TODO: It would be nice, if such an update could also contain information about the type of
 * changes (added, removed, updated statements).
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLDAOUpdatedEvent extends AbstractDAOEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public SPARQLDAOUpdatedEvent(Object source) {
    super(source, Instant.now());
  }

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   * @param timestamp the {@link Instant}, when the even occured (never {@code null})
   */
  public SPARQLDAOUpdatedEvent(Object source, Instant timestamp) {
    super(source, timestamp);
  }

}
