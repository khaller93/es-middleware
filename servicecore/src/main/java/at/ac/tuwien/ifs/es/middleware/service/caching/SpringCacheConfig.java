package at.ac.tuwien.ifs.es.middleware.service.caching;

import java.util.Arrays;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCaching
public class SpringCacheConfig {

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(Arrays.asList(
        new ConcurrentMapCache("sparql"),
        new ConcurrentMapCache("fts"),
        new ConcurrentMapCache("gremlin"),
        new ConcurrentMapCache("esm.service.analytics.dataset.class.hierarchy")));
    return cacheManager;
  }

}
