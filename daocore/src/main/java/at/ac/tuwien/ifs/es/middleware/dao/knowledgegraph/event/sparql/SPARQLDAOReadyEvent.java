package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql;

import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when the SPARQL DAO is ready for usage and no known changes were
 * made to the data of the underlying database.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLDAOReadyEvent extends ApplicationEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public SPARQLDAOReadyEvent(Object source) {
    super(source);
  }
}
