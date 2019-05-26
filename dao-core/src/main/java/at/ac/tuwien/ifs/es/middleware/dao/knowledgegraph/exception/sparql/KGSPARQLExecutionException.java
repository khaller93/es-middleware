package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql;

/**
 * This exception shall be thrown, if a given SPARQL query cannot be executed and the client passing
 * an invalid query is not the cause. In this case {@link KGMalformedSPARQLQueryException} must be
 * thrown.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGSPARQLExecutionException extends KGSPARQLException {

  public KGSPARQLExecutionException() {
  }

  public KGSPARQLExecutionException(String message) {
    super(message);
  }

  public KGSPARQLExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGSPARQLExecutionException(Throwable cause) {
    super(cause);
  }

  public KGSPARQLExecutionException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
