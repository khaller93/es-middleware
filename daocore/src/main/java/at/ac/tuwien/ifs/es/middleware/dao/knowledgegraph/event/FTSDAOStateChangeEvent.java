package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;
import java.time.Instant;

public class FTSDAOStateChangeEvent extends DAOStateChangeEvent {

  public FTSDAOStateChangeEvent(KGDAO source, long eventId,
      KGDAOStatus newStatus, KGDAOStatus prevStatus, Instant timestamp) {
    super(source, eventId, newStatus, prevStatus, timestamp);
  }

  public FTSDAOStateChangeEvent(KGFullTextSearchDAO source,
      KGDAOStatus newStatus, KGDAOStatus prevStatus, Instant timestamp) {
    super(source, newStatus, prevStatus, timestamp);
  }
}
