package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql;

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
public class SPARQLDAOUpdatingEvent extends ApplicationEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public SPARQLDAOUpdatingEvent(Object source) {
    super(source);
  }
}
