package at.ac.tuwien.ifs.es.middleware.dto.exploration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Instances of this interface represent an intermediate or final result of an exploration flow.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.MINIMAL_CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class")
public abstract class ExplorationContext {

  /**
   * Returns a deep copy of this exploration result.
   *
   * @return a deep copy of this exploration result.
   */
  public abstract <C extends ExplorationContext> C deepCopy();

}
