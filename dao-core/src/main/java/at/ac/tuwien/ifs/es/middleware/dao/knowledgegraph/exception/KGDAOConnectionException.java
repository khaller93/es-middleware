package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception;

/**
 * This exception shall be thrown, if no connection to knowledge graph can be setup.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGDAOConnectionException extends KGDAOException {

  public KGDAOConnectionException() {
  }

  public KGDAOConnectionException(String message) {
    super(message);
  }

  public KGDAOConnectionException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGDAOConnectionException(Throwable cause) {
    super(cause);
  }

}
