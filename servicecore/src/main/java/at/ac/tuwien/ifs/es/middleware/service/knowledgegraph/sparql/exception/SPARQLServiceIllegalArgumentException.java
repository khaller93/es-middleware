package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SPARQLServiceIllegalArgumentException extends RuntimeException {

  public SPARQLServiceIllegalArgumentException() {
  }

  public SPARQLServiceIllegalArgumentException(String message) {
    super(message);
  }

  public SPARQLServiceIllegalArgumentException(String message, Throwable cause) {
    super(message, cause);
  }

  public SPARQLServiceIllegalArgumentException(Throwable cause) {
    super(cause);
  }

  public SPARQLServiceIllegalArgumentException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
