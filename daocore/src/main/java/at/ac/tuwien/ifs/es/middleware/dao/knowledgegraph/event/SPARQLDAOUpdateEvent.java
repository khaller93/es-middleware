package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when data was changed in the underlying database and the
 * publishing {@link KnowledgeGraphDAO} has incorporated those changes. It is an {@link
 * ApplicationEvent} that an {@link org.springframework.context.ApplicationListener} can listen to.
 * <p/>
 * It would be nice, if such an update could also contain information about the type of changes
 * (added, removed, updated statements).
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLDAOUpdateEvent extends ApplicationEvent {

  /**
   * Create a new {@link SPARQLDAOUpdateEvent}.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public SPARQLDAOUpdateEvent(Object source) {
    super(source);
  }
}
