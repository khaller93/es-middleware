package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception;

/**
 * This exception shall be thrown, if DAO to knowledge graph cannot be setup.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGDAOSetupException extends KGDAOException {

  public KGDAOSetupException() {
  }

  public KGDAOSetupException(String message) {
    super(message);
  }

  public KGDAOSetupException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGDAOSetupException(Throwable cause) {
    super(cause);
  }

}
