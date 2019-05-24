package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception;

/**
 * Marker exception for all exception originating in the DAO layer.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class KGDAOException extends RuntimeException {

  public KGDAOException() {
  }

  public KGDAOException(String message) {
    super(message);
  }

  public KGDAOException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGDAOException(Throwable cause) {
    super(cause);
  }

  public KGDAOException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
