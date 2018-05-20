package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when data was changed in the underlying database and the
 * publishing {@link FullTextSearchDAO} has incorporated those changes. It is an {@link
 * ApplicationEvent} that an {@link org.springframework.context.ApplicationListener} can listen to.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class FullTextSearchDAOUpdateEvent extends ApplicationEvent {

  /**
   * Create a new {@link FullTextSearchDAOUpdateEvent}.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public FullTextSearchDAOUpdateEvent(Object source) {
    super(source);
  }

}
