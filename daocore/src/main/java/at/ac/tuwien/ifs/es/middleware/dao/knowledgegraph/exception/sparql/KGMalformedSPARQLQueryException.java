package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.sparql;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;

/**
 * This exception shall be thrown, if the client handed over a malformed query to the
 * {@link KGSparqlDAO}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGMalformedSPARQLQueryException extends KGSPARQLException {

  public KGMalformedSPARQLQueryException() {
  }

  public KGMalformedSPARQLQueryException(String message) {
    super(message);
  }

  public KGMalformedSPARQLQueryException(String message, Throwable cause) {
    super(message, cause);
  }

  public KGMalformedSPARQLQueryException(Throwable cause) {
    super(cause);
  }
}
