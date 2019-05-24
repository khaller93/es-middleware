package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

/**
 * Instances of this interface represent results that can be identified with a certain unique string
 * value.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface IdentifiableResult {

  /**
   * Gets the string value that identifies this result.
   *
   * @return the string value identifying this result, must not be null.
   */
  String getId();

}
