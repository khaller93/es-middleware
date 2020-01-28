package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOConnectionException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOException;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.exception.KGDAOSetupException;

/**
 * Instances of this interface represent a DAO to the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KGDAO {

  /**
   * Setups this DAO to the knowledge graph.
   *
   * @throws KGDAOConnectionException if connection fails.
   * @throws KGDAOSetupException if the setup generally fails.
   */
  void setup() throws KGDAOSetupException, KGDAOConnectionException;

  /**
   * Updates this DAO to the knowledge graph. This method  shall be called, if it is suspected
   * that the managed knowledge graph has been changed.
   *
   * @param timestamp of the update.
   * @throws KGDAOConnectionException if connection fails.
   * @throws KGDAOException if the update generally fails.
   */
  void update(long timestamp) throws KGDAOException;

}
