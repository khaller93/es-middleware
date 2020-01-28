package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * This configuration provides a thread pool ({@link TaskExecutor}) for the application.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class ThreadPoolConfig {

  @Value("${esm.pool.core.threads:4}")
  private int corePoolSize;
  @Value("${esm.pool.max.threads:4}")
  private int maxPoolSize;

  @Bean
  @Primary
  public TaskExecutor threadPool() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setThreadNamePrefix("esm_threadpool");
    executor.initialize();
    return executor;
  }

}
