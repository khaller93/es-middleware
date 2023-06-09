package at.ac.tuwien.ifs.es.middleware.service.analysis;

import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.FullTextSearchService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SPARQLService;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a {@link AnalysisService} class for being registered in the analysis
 * processing pipeline. An analysis service can require a {@link SPARQLService},
 * {@link GremlinService} and/or
 * {@link FullTextSearchService} for
 * computing its function. Moreover, this service might have the successful completion of other
 * analysis services as prerequisites. All this information must be specified in the annotation.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterForAnalyticalProcessing {

  /**
   * The unique name get the analytical processing.
   *
   * @return unique name get the analytical processing.
   */
  String name();

  /**
   * Indicates, whether this services needs the {@link SPARQLService}
   * to be ready.
   *
   * @return {@code true}, if this service requires {@link SPARQLService}
   * to be ready, otherwise {@code false}.
   */
  boolean requiresSPARQL() default false;

  /**
   * Indicates, whether this services needs the {@link FullTextSearchService}
   * to be ready.
   *
   * @return {@code true}, if this service requires {@link FullTextSearchService}
   * * to be ready, otherwise {@code false}.
   */
  boolean requiresFullTextSearch() default false;

  /**
   * Indicates, whether this services needs the {@link GremlinService}
   * to be ready.
   *
   * @return {@code true}, if this service requires {@link GremlinService}
   * to be ready, otherwise {@code false}.
   */
  boolean requiresGremlin() default false;

  /**
   * An array of {@link AnalysisService} that are needed as prerequisites for computing this
   * service.
   *
   * @return an array of {@link AnalysisService} that are needed as prerequisites for computing this
   * service.
   */
  Class<? extends AnalysisService>[] prerequisites() default {};

  /**
   * If {@code true}, this analysis service is not put into the analysis pipeline, but registered.
   * This setting can be overwritten with Spring properties by setting {@code
   * esm.analysis.processor.disable.xxx} to {@code true} or {@code false}, where {@code xxx} is the
   * registered name ({@link RegisterForAnalyticalProcessing#name()}).
   *
   * @return {@code true}, if this analysis services should not be put into the analysis pipeline,
   * otherwise {@code false}.
   */
  boolean disabled() default false;

}
