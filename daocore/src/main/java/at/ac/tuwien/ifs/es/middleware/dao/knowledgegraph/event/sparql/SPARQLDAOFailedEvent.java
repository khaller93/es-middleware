package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import org.springframework.context.ApplicationEvent;

/**
 * This event shall be triggered, when the {@link KGGremlinDAO} fails to become operational.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class SPARQLDAOFailedEvent extends ApplicationEvent {

  private String message;
  private Exception exception;

  /**
   * @param source that issued the event.
   * @param message {@link String} describing the failure briefly, can be {@code null}.
   * @param exception {@link Exception} that has been thrown, can be {@code null}.
   */
  public SPARQLDAOFailedEvent(Object source, String message, Exception exception) {
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
