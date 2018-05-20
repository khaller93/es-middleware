package at.ac.tuwien.ifs.es.middleware.service.exploration.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class PreComputationRegistry {

  private static final Logger logger = LoggerFactory.getLogger(PreComputationRegistry.class);



}
