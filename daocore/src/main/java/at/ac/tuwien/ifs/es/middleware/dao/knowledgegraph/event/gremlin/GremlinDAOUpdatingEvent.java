package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when data was changed in the underlying database and the {@link
 * KGGremlinDAO} incorporates these
 * changes right now. It is an {@link ApplicationEvent} that an {@link
 * org.springframework.context.ApplicationListener} can listen to.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class GremlinDAOUpdatingEvent extends ApplicationEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public GremlinDAOUpdatingEvent(Object source) {
    super(source);
  }
}
