package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
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
public class PrimaryKGDAOConfig implements KnowledgeGraphDAOConfig {

  private KnowledgeGraphDAOConfig config;

  @Autowired
  public PrimaryKGDAOConfig(ApplicationContext context, @Value("${esm.db.choice}") String choice) {
    this.config = context.getBean(choice,KnowledgeGraphDAOConfig.class);
  }

  @Bean
  @Primary
  @Override
  public KGSparqlDAO getSparqlDAO() {
    return config.getSparqlDAO();
  }

  @Bean
  @Primary
  @Override
  public KGFullTextSearchDAO getFullTextSearchDAO() {
    return config.getFullTextSearchDAO();
  }


  @Bean
  @Primary
  @Override
  public KGGremlinDAO getGremlinDAO() {
    return config.getGremlinDAO();
  }
}
