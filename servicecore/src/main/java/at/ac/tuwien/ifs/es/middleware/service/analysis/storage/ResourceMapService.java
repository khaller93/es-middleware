package at.ac.tuwien.ifs.es.middleware.service.analysis.storage;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class ResourceMapService {

  private final ResourceEntityRepository resourceEntityRepository;

  private final Map<String, ResourceEntity> resourceCache = new ConcurrentHashMap<>();

  @Autowired
  public ResourceMapService(ResourceEntityRepository resourceEntityRepository) {
    this.resourceEntityRepository = resourceEntityRepository;
  }

  @PostConstruct
  public void setUp() {
    resourceEntityRepository.findAll().forEach(re -> resourceCache.put(re.getResourceId(), re));
  }


  public ResourceEntity get(Resource resource) {
    return get(resource.getId());
  }

  @Cacheable(cacheNames = {"centrality"})
  public ResourceEntity get(String resourceId) {
    return resourceEntityRepository.findByResourceId(resourceId)
        .orElseGet(() -> resourceEntityRepository.save(new ResourceEntity(resourceId)));
  }


  public List<ResourceEntity> pushAndGet(List<String> resourceId){
    
  }


}
