package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

/**
 * This configuration prepares the certain {@link FullTextSearchDAO} that is specified in the
 * application properties. The relevant property is {@code esm.fts.choice}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class FullTextSearchConfig {

  @Value("${esm.fts.choice:#{null}}")
  private String choice;

  @Lazy
  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public FullTextSearchDAO getSpecificFullTextSearchDAO(@Autowired ApplicationContext context) {
    if (choice == null) {
      throw new IllegalStateException("There was no choice for full-text-search given.");
    }
    return context.getBean(choice, FullTextSearchDAO.class);
  }

}
