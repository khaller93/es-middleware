package at.ac.tuwien.ifs.es.middleware.service.sparql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * A configuration for the SPARQL service. The user of this middleware can decide whether caching
 * shall be used or not.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@EnableCaching
@Configuration
public class SPARQLServiceConfig {

  @Value("${esm.cache.enable:false}")
  private boolean enableCaching;

  @Bean
  @Primary
  public SPARQLService getSpecificSPARQLService(@Autowired ApplicationContext context) {
    return context.getBean(enableCaching ? "CachedSPARQLService" : "SimpleSPARQLService",
        SPARQLService.class);
  }

}
