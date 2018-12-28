package at.ac.tuwien.ifs.es.middleware.service.analysis.similarity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * This repository provides methods to store and fetch {@link SimilarityMetricResult}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface SimilarityMetricStoreService extends
    CrudRepository<SimilarityMetricResult, SimilarityMetricKey> {

}
