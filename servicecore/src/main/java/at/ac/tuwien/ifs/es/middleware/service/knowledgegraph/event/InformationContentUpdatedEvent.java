package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.event;

import org.springframework.context.ApplicationEvent;

public class InformationContentUpdatedEvent extends ApplicationEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public InformationContentUpdatedEvent(Object source) {
    super(source);
  }
}
