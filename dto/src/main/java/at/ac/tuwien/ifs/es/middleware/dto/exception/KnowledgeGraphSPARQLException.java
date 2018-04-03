package at.ac.tuwien.ifs.es.middleware.dto.exception;

/**
 * This exception is a marker exception for exceptions occuring in a {@link
 * at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO} concerned with of SPARQL
 * queries.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KnowledgeGraphSPARQLException extends KnowledgeGraphDAOException {

  public KnowledgeGraphSPARQLException() {
  }

  public KnowledgeGraphSPARQLException(String message) {
    super(message);
  }

  public KnowledgeGraphSPARQLException(String message, Throwable cause) {
    super(message, cause);
  }

  public KnowledgeGraphSPARQLException(Throwable cause) {
    super(cause);
  }

}
