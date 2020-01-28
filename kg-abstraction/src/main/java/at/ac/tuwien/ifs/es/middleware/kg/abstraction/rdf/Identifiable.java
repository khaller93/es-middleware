package at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf;

/**
 * Instances of this interface represent results that can be identified with a certain string value.
 * Two {@link Identifiable}s are the same, if their string ids are the same.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface Identifiable {

  /**
   * Gets the string value that identifies this result.
   *
   * @return the string value identifying this result, must not be {@code null}.
   */
  String getId();

}
