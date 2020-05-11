package at.ac.tuwien.ifs.es.middleware.service.analysis.value;

/**
 * A result of an analysis, instances of this interface encapsulate the value of the result.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AnalysisNumberValue<V> {

  /**
   * Gets the value of the .
   *
   * @return the value.
   */
  V getValue();

}
