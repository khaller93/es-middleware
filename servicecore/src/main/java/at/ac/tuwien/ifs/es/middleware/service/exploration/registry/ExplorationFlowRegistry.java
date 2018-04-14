package at.ac.tuwien.ifs.es.middleware.service.exploration.registry;

import at.ac.tuwien.ifs.es.middleware.service.exploration.factory.DynamicExplorationFlowFactory;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlowStep;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * This class is the central registry for {@link ExplorationFlowStep}s that is considered by {@link
 * DynamicExplorationFlowFactory} instances for constructing the workflow dynamically.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class ExplorationFlowRegistry {

  private static final Logger logger = LoggerFactory.getLogger(ExplorationFlowRegistry.class);

  private ConcurrentMap<String, Class<? extends ExplorationFlowStep>> registry = new ConcurrentHashMap<>();

  /**
   * Registers the given {@link ExplorationFlowStep} class under the given {@code uid}.
   *
   * @param uid the unique name under which the class shall be registered.
   * @param clazz {@link ExplorationFlowStep} class that shall be registered under the given {@code
   * uid}.
   */
  public void register(String uid, Class<? extends ExplorationFlowStep> clazz) {
    Class registeredClazz = registry.putIfAbsent(uid, clazz);
    if (registeredClazz == clazz) {
      logger.warn("The given class '%s' could not registered under the name '%s'.", clazz, uid);
      //TODO: throw Runtime Exception.
    }
    logger.info("The exploration step '%s' has been registered under the name '%s'.", clazz, uid);
  }

  /**
   * Gets the {@link ExplorationFlowStep} class with the given {@code uid}. If no such class is
   * registered, {@link Optional#EMPTY} will be returned.
   *
   * @param uid the unique name of the {@link ExplorationFlowStep} clazz that shall be returned.
   * @return the class registered for the given {@code uid}, or {@link Optional#EMPTY}, if there is
   * no registration under this name.
   */
  public Optional<Class<? extends ExplorationFlowStep>> get(String uid) {
    Class<? extends ExplorationFlowStep> aClass = registry.get(uid);
    return aClass != null ? Optional.of(aClass) : Optional.empty();
  }

  /**
   * Removes the {@link ExplorationFlowStep} with the given {@code uid} from the registry.
   *
   * @param uid the unique name of the {@link ExplorationFlowStep} that shall be removed.
   */
  public void unregister(String uid) {
    registry.remove(uid);
  }

}
