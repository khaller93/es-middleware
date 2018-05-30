package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.fts;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when data was changed in the underlying database and the {@link
 * KGFullTextSearchDAO} incorporates these changes right now. It is an {@link ApplicationEvent} that
 * an {@link org.springframework.context.ApplicationListener} can listen to.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class FullTextSearchDAOUpdatingEvent extends ApplicationEvent {

  /**
   * Create a new {@link FullTextSearchDAOUpdatingEvent}.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public FullTextSearchDAOUpdatingEvent(Object source) {
    super(source);
  }

}
