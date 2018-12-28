package at.ac.tuwien.ifs.es.middleware.dto.status;

/**
 * This class represents a ready {@link KGDAOStatus}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGDAOReadyStatus extends KGDAOStatus {

  public KGDAOReadyStatus() {
    super(CODE.READY);
  }
}
