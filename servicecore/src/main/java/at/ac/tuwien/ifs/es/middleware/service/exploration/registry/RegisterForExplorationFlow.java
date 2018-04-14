package at.ac.tuwien.ifs.es.middleware.service.exploration.registry;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be applied to types that shall be registered at {@link
 * ExplorationFlowRegistry}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterForExplorationFlow {

  /**
   * Unique name under which this type should be registered at {@link ExplorationFlowRegistry}.
   */
  String value();
}
