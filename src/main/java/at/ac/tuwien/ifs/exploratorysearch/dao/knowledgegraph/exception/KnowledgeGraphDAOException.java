package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception;

/**
 * Marker exception for all exception originating from the {@link at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.KnowledgeGraphDAO}.
 */
public class KnowledgeGraphDAOException extends RuntimeException {

  public KnowledgeGraphDAOException() {
  }

  public KnowledgeGraphDAOException(String message) {
    super(message);
  }

  public KnowledgeGraphDAOException(String message, Throwable cause) {
    super(message, cause);
  }

  public KnowledgeGraphDAOException(Throwable cause) {
    super(cause);
  }
}
