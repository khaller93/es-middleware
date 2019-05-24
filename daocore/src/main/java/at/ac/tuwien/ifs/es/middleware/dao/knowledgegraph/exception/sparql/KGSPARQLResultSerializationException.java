package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql;

/**
 * This exception shall be thrown, if the serialization of a {@link at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.sparql.QueryResult}
 * fails.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGSPARQLResultSerializationException extends KGSPARQLException {

  public KGSPARQLResultSerializationException() {
  }

  public KGSPARQLResultSerializationException(String message) {
    super(message);
  }

  public KGSPARQLResultSerializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGSPARQLResultSerializationException(Throwable cause) {
    super(cause);
  }
}
