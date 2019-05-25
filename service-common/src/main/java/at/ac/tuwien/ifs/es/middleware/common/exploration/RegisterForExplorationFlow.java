package at.ac.tuwien.ifs.es.middleware.common.exploration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation identifies an exploration flow step that shall be registered. It must only be
 * applied to exploration flow step implementations.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterForExplorationFlow {

  /**
   * Unique name under which this type should be registered.
   *
   * @return name under which this type should be registered.
   */
  String value();
}
