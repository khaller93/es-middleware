package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

/**
 * This configuration prepares the certain {@link KnowledgeGraphDAO} that is specified in the
 * application properties. The relevant property is {@code esm.knowledgegraph.choice}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class KnowledgeGraphConfig {

  @Value("${esm.db.choice}")
  private String choice;

  @Bean
  @Primary
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public KnowledgeGraphDAO getSpecifiedKnowledgeGraphDAO(@Autowired ApplicationContext context) {
    return context.getBean(choice, KnowledgeGraphDAO.class);
  }

}
