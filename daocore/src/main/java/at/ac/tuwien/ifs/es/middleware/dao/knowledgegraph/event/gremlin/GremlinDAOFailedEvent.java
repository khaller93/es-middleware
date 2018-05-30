package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when the {@link KGGremlinDAO} fails to become operational.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class GremlinDAOFailedEvent extends ApplicationEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public GremlinDAOFailedEvent(Object source) {
    super(source);
  }
}
