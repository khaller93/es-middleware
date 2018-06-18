package at.ac.tuwien.ifs.es.middleware.dto.status;

/**
 * This class represents an initiating {@link KGDAOStatus}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class KGDAOInitStatus extends KGDAOStatus {

  public KGDAOInitStatus() {
    super(CODE.INITIATING);
  }
}
