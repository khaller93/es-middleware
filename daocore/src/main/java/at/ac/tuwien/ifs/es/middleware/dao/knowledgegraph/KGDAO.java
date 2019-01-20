package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus;

/**
 * Instances of this interface represent a DAO to the knowledge graph. It is expected from a DAO
 * that it is reporting its status.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KGDAO {

  /**
   * Gets the current {@link KGDAOStatus} of this DAO.
   *
   * @return current {@link KGDAOStatus} of this DAO.
   */
  KGDAOStatus getStatus();

}