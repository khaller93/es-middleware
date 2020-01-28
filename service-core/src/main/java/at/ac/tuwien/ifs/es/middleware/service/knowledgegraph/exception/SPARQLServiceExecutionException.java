package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class SPARQLServiceExecutionException extends RuntimeException {

  public SPARQLServiceExecutionException() {
  }

  public SPARQLServiceExecutionException(String message) {
    super(message);
  }

  public SPARQLServiceExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public SPARQLServiceExecutionException(Throwable cause) {
    super(cause);
  }

  public SPARQLServiceExecutionException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
