package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * This configuration prepares the {@link KnowledgeGraphDAO} that is specified in the application
 * properties for usage. The relevant property is {@code es.middleware.knowledgegraph.vendor}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class KnowledgeGraphDAOConfig {

  private static final Logger logger = LoggerFactory.getLogger(KnowledgeGraphDAO.class);

  @Value("${es.middleware.knowledgegraph.vendor}")
  private String vendor;

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public KnowledgeGraphDAO SpecifiedKnowledgeGraphDAO(@Autowired ApplicationContext context) {
    if (vendor == null) {
      logger.warn(
          "No vendor was given in the application properties for KnowledgegraphDAO. Fallback to RDF4J in-memory triplestore.");
      vendor = "MemoryDB";
    }
    return (KnowledgeGraphDAO) context.getBean(vendor);
  }

}
