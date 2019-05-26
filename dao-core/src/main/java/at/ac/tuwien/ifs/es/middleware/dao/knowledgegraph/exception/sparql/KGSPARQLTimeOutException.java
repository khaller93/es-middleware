package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql;

/**
 * This exception shall be thrown, if the execution of a SPARQL query timed out.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGSPARQLTimeOutException extends KGSPARQLException {

  public KGSPARQLTimeOutException() {
  }

  public KGSPARQLTimeOutException(String message) {
    super(message);
  }

  public KGSPARQLTimeOutException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGSPARQLTimeOutException(Throwable cause) {
    super(cause);
  }

}
