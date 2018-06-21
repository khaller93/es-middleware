package at.ac.tuwien.ifs.es.middleware.service.integration.knowledegraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.stereotype.Component;

@Component
public class CentralityCacheManagerStub implements CacheManager {

  private Cache cache = new ConcurrentMapCache("centrality");

  @Override
  public Cache getCache(String name) {
    return cache;
  }

  @Override
  public Collection<String> getCacheNames() {
    return Collections.singleton("centrality");
  }
}
