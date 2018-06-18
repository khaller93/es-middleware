package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * This conf prepares the certain {@link KnowledgeGraphDAOConfig} that is specified in the
 * application properties. The relevant property is {@code esm.knowledgegraph.choice}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Primary
@Configuration
public class KGDAOConfig implements KnowledgeGraphDAOConfig {

  private KnowledgeGraphDAOConfig config;

  @Autowired
  public KGDAOConfig(ApplicationContext context, @Value("${esm.db.choice}") String choice) {
    this.config = context.getBean(choice,KnowledgeGraphDAOConfig.class);
  }

  @Primary
  @Bean
  @Override
  public KGSparqlDAO getSparqlDAO() {
    return config.getSparqlDAO();
  }

  @Primary
  @Bean
  @Override
  public KGFullTextSearchDAO getFullTextSearchDAO() {
    return config.getFullTextSearchDAO();
  }

  @Primary
  @Bean
  @Override
  public KGGremlinDAO getGremlinDAO() {
    return config.getGremlinDAO();
  }
}
