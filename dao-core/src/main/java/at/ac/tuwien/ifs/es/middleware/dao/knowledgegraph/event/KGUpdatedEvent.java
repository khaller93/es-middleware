package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event;

import org.springframework.context.ApplicationEvent;

/**
 *
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGUpdatedEvent extends ApplicationEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public KGUpdatedEvent(Object source) {
    super(source);
  }
}
