package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.exception;

/**
 * This exception shall be thrown, if no connection to knowledge graph can be setup.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KnowledgeGraphSetupException extends KnowledgeGraphDAOException {

  public KnowledgeGraphSetupException() {
  }

  public KnowledgeGraphSetupException(String message) {
    super(message);
  }

  public KnowledgeGraphSetupException(String message, Throwable cause) {
    super(message, cause);
  }

  public KnowledgeGraphSetupException(Throwable cause) {
    super(cause);
  }

}
