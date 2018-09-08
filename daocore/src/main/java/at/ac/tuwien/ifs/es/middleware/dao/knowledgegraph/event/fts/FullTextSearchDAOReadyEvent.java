package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.fts;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.AbstractDAOEvent;
import java.time.Instant;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when data was changed in the underlying database and the
 * publishing {@link KGFullTextSearchDAO} has incorporated those changes. It is an {@link
 * ApplicationEvent} that an {@link org.springframework.context.ApplicationListener} can listen to.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class FullTextSearchDAOReadyEvent extends AbstractDAOEvent {

  /**
   * Create a new {@link FullTextSearchDAOReadyEvent}.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public FullTextSearchDAOReadyEvent(Object source) {
    super(source, Instant.now());
  }

  public FullTextSearchDAOReadyEvent(Object source, Instant timestamp) {
    super(source, timestamp);
  }
}
