package at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality;

import at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality.entity.CentralityMetricKey;
import at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality.entity.CentralityMetricResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * This repository provides methods to storage and fetch {@link CentralityMetricResult}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CentralityMetricStoreRepository extends
    CrudRepository<CentralityMetricResult, CentralityMetricKey> {


  @Query("select c from CentralityMetricResult c where c.key.centralityUID = :centralityUid and c.key.resourceEntity.resourceId = :resourceId")
  Optional<CentralityMetricResult> findByMetricAndResource(String centralityUid, String resourceId);

}
