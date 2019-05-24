package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql;

/**
 * This exception shall be thrown, if no connection to knowledge graph can be setup.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGSPARQLSetupException extends KGSPARQLException {

  public KGSPARQLSetupException() {
  }

  public KGSPARQLSetupException(String message) {
    super(message);
  }

  public KGSPARQLSetupException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGSPARQLSetupException(Throwable cause) {
    super(cause);
  }

}
