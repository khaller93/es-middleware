package at.ac.tuwien.ifs.es.middleware.dto.status;

/**
 * This class represents an initiating {@link KGDAOUpdatingStatus}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGDAOUpdatingStatus extends KGDAOStatus {

  public KGDAOUpdatingStatus() {
    super(CODE.UPDATING);
  }
}
