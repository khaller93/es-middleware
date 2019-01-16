package at.ac.tuwien.ifs.es.middleware.service.analysis.centrality;

import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.entity.CentralityMetricKey;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.entity.CentralityMetricResult;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * This repository provides methods to store and fetch {@link CentralityMetricResult}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CentralityMetricStoreService extends
    CrudRepository<CentralityMetricResult, CentralityMetricKey> {

}
