package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;

/**
 * This exception is a marker exception for exceptions occurring in a {@link
 * at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class KGSPARQLException extends KGDAOException {

  public KGSPARQLException() {
  }

  public KGSPARQLException(String message) {
    super(message);
  }

  public KGSPARQLException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGSPARQLException(Throwable cause) {
    super(cause);
  }

  public KGSPARQLException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
