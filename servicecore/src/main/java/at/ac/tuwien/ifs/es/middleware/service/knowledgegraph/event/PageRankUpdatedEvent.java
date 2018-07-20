package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.event;

import org.springframework.context.ApplicationEvent;

/**
 * This event will be published, when the page rank has been updated and is ready.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class PageRankUpdatedEvent extends ApplicationEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public PageRankUpdatedEvent(Object source) {
    super(source);
  }

}
