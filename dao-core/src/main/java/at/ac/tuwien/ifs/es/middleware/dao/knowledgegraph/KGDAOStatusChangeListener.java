package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus;

/**
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface KGDAOStatusChangeListener {

  /**
   * This method is called, if the status of the {@link KGDAO} changes.
   *
   * @param newStatus new {@link KGDAOStatus} of the {@link KGDAO}.
   */
  void onStatusChange(KGDAOStatus newStatus);

}
