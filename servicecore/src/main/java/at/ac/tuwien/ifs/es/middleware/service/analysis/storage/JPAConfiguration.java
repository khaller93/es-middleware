package at.ac.tuwien.ifs.es.middleware.service.analysis.storage;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("at.ac.tuwien.ifs.es.middleware.service.analysis.storage")
@EnableTransactionManagement
@EntityScan("at.ac.tuwien.ifs.es.middleware.service.analysis.storage")
public class JPAConfiguration {

}
