package at.ac.tuwien.ifs.es.middleware.service.analysis.event;

import org.springframework.context.ApplicationEvent;

/**
 *
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class AnalysisServiceUpdatedEvent extends ApplicationEvent {

  /**
   * Create a new {@link AnalysisServiceUpdatedEvent}.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public AnalysisServiceUpdatedEvent(Object source) {
    super(source);
  }

}
