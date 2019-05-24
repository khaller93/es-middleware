package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus;
import java.time.Instant;

public class SparqlDAOStateChangeEvent extends DAOStateChangeEvent {

  public SparqlDAOStateChangeEvent(KGDAO source,
      long eventId, KGDAOStatus newStatus,
      KGDAOStatus prevStatus, Instant timestamp) {
    super(source, eventId, newStatus, prevStatus, timestamp);
  }

  public SparqlDAOStateChangeEvent(KGSparqlDAO source,
      KGDAOStatus newStatus, KGDAOStatus prevStatus, Instant timestamp) {
    super(source, newStatus, prevStatus, timestamp);
  }
}
