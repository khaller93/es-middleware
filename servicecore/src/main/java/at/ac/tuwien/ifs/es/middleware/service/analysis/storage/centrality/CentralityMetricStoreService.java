package at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.storage.ResourceMapService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality.entity.CentralityMetricKey;
import at.ac.tuwien.ifs.es.middleware.service.analysis.storage.centrality.entity.CentralityMetricResult;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This repository provides methods to storage and fetch {@link CentralityMetricResult}s.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class CentralityMetricStoreService  {

  private ResourceMapService resourceMapService;
  private CentralityMetricStoreRepository centralityMetricStoreRepository;

  @Autowired
  public CentralityMetricStoreService(
      ResourceMapService resourceMapService,
      CentralityMetricStoreRepository centralityMetricStoreRepository) {
    this.resourceMapService = resourceMapService;
    this.centralityMetricStoreRepository = centralityMetricStoreRepository;
  }

  public CentralityMetricResult get(String centralityUID, Resource resource, Number value) {
    return new CentralityMetricResult(
        CentralityMetricKey.of(centralityUID, resourceMapService.get(resource)), value);
  }

  public CentralityMetricResult get(String centralityUID, String resource, Number value) {
    return new CentralityMetricResult(
        CentralityMetricKey.of(centralityUID, resourceMapService.get(resource)), value);
  }


  public Optional<CentralityMetricResult> findById(String centralityUID, Resource resource) {
    return findById(centralityUID, resource.getId());
  }

  public Optional<CentralityMetricResult> findById(String centralityUID, String resourceId) {
    return centralityMetricStoreRepository.findByMetricAndResource(centralityUID, resourceId);
  }

}
