package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus;
import java.time.Instant;

public class GremlinDAOStateChangeEvent extends DAOStateChangeEvent {

  public GremlinDAOStateChangeEvent(KGDAO source,
      long eventId, KGDAOStatus newStatus,
      KGDAOStatus prevStatus, Instant timestamp) {
    super(source, eventId, newStatus, prevStatus, timestamp);
  }

  public GremlinDAOStateChangeEvent(KGGremlinDAO source,
      KGDAOStatus newStatus, KGDAOStatus prevStatus, Instant timestamp) {
    super(source, newStatus, prevStatus, timestamp);
  }
}
