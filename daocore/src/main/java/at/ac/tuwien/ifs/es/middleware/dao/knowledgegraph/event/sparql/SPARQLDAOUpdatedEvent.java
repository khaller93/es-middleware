package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when data has been changed in the underlying database and the
 * publishing {@link KnowledgeGraphDAO} has incorporated those changes. It is an {@link
 * ApplicationEvent} that an {@link org.springframework.context.ApplicationListener} can listen to.
 * <p/>
 * //TODO: It would be nice, if such an update could also contain information about the type of changes (added, removed, updated statements).
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLDAOUpdatedEvent extends ApplicationEvent {

  /**
   * Create a new {@link SPARQLDAOUpdatedEvent}.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public SPARQLDAOUpdatedEvent(Object source) {
    super(source);
  }
}
