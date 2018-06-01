package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.fts;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when the {@link KGGremlinDAO} fails to become operational.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class FullTextSearchDAOFailedEvent extends ApplicationEvent {

  private String message;
  private Exception exception;

  /**
   * Creates a new {@link FullTextSearchDAOFailedEvent} with the specified information.
   *
   * @param source that issued the event.
   * @param message that describes the error briefly.
   * @param exception {@link Exception} that caused the error.
   */
  public FullTextSearchDAOFailedEvent(Object source, String message, Exception exception) {
    super(source);
    this.message = message;
    this.exception = exception;
  }

  public String getMessage() {
    return message;
  }

  public Exception getException() {
    return exception;
  }
}
