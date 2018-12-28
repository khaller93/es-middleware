package at.ac.tuwien.ifs.es.middleware.service.analysis;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public @interface AnalyticalProcessing {

  /**
   * The unique name of the analytical processing.
   *
   * @return unique name of the analytical processing.
   */
  String name();

  /**
   *
   * @return
   */
  boolean online() default false;

}
