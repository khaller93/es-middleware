package at.ac.tuwien.ifs.es.middleware.service.fts;

import at.ac.tuwien.ifs.es.middleware.service.sparql.SPARQLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * A configuration for the full-text-search service. The user of this middleware can decide whether
 * caching shall be used or not.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class FullTextSearchServiceConfig {

  @Value("${esm.cache.enable:false}")
  private boolean enableCaching;

  @Primary
  @Bean
  public FullTextSearchService getFullTextSearchService(@Autowired ApplicationContext context) {
    return context
        .getBean(enableCaching ? "CachedFullTextSearchService" : "SimpleFullTextSearchService",
            FullTextSearchService.class);
  }

}
